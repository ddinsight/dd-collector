# -*- coding: utf-8 -*-
#
#  Copyright 2015 AirPlug Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


import re
import log

"""
field = {"fmt" : <field formatter>
        "cond" : [<type>, <condition>] --> dep 참고
        "opt" : 0 or 1 # Optional field?
        "dep" : {<field name>:[<type>, <condition>], }
                # opt == 1일때, 이 필드의 부모 필드. 없으면 생략
                # type -> type, val, regex, range, present
                    "type" : "cond":["type", IntegerType]
                    or "cond":["val", 10]
                    or "cond":["regex", "^WIFI|^mobile"]
                    or "cond":["range", 0, 100] --> min/max
                    or "cond":["present", boolean] --> True/False
                    or "cond":["in", [val1, val2, val2]]] --> one of them


}

Step.
1. fmt() 함수 호출하여 값 변환
2. cond 조건에 따라 값 체크
3. opt 필드 체크
4. dep 필드에 명시된 필드들의 값을 체크하여 체크

결과값 저장
_validityResult = {<fieldName> : [<val>, <result code>, <msg>]}

result code :
    - 0 : ok
    - 10 : field value error
    - 20 : mandatory field error (필수 필드인데 없을때)
    - 30 : Dependency error
    - 40 : unknown field

"""

"""
Container decorator

필드명 변경이 필요한 경우에는 아래와 같은 형식으로 class 선언부 앞에 쓴다. 
-> @Container({oldName:newName,...})

필요없는 경우에는  아래와 같은 형식으로 class 선언부 앞에 쓴다.
-> @Container
"""
ERR_NONE = "OK"
ERR_NORMAL = "ERR_NORMAL"
ERR_FIELD_VALUE = "ERR_VALUE"
ERR_FIELD_NOT_FOUND = "ERR_FIELD"
ERR_DEPENDENCY = "ERR_DEPENDENCY"
ERR_UNKNOWN_FIELD = "UNKNOWN_FIELD"


def Container(param=None, report=False, warn=False):
    class BaseContainer(dict):
        def __init__(self, *args, **kwargs):
            if self.__makeReport__:
                self.__validityReport = {}

            self.update(*args, **kwargs)

        def __addReport(self, key, elem):
            if self.__makeReport__:
                report = self.__validityReport.get(key, [])
                report.append(tuple(elem))
                self.__validityReport[key] = report

        def __getValidityMessage(self, cond, val, key=None):
            if cond[0] == "present":
                if not key:
                    return "key field name is not present"

                if self.has_key(key) != cond[1]:
                    return "%s field must be %s" % "present" if cond[1] else "absent"
            else:
                if key and not self.has_key(key):
                    return "%s field not found" % key

                if cond[0] == "type":
                    if not isinstance(val, cond[1]):
                        return "type mismatch"
                elif cond[0] == "val":
                    if val != cond[1]:
                        return "value error %s != %s" % (val, cond[1])
                elif cond[0] == "regex":
                    if not re.match(cond[1], val):
                        return "regex error %s" % (val)
                elif cond[0] == "range":
                    if val < cond[1] or cond[2] < val:
                        return "range error - %s is not in [%s ~ %s]" % (val, cond[1], cond[2])
                elif cond[0] == "in":
                    if not isinstance(cond[1], list):
                        return "filter error - %s - %s" % (cond[0], cond[1])

                    if val not in cond[1]:
                        return "value error - %s not in %s" % (cond[0], cond[1])

            return None

        def __setitem__(self, key, val):
            if hasattr(self.__fieldFormat__, key):
                """
                if val == None:
                    self.__addReport(key, (val, ERR_FIELD_VALUE, "null value"))
                    return
                """

                try:
                    attr = getattr(self.__fieldFormat__, key)
                    if isinstance(attr, dict):
                        val = attr.get('fmt')(val)
                        if attr.has_key('cond'):
                            msg = self.__getValidityMessage(attr['cond'], val)
                            if msg:
                                self.__addReport(key, (val, ERR_FIELD_VALUE, msg))
                    else:
                        val = attr(val)
                    if val == None:
                        self.__addReport(key, (val, ERR_FIELD_VALUE, "null value"))
                        return
                except Exception, e:
                    log.warn("Param type error : %s - %s, %s" % (key, val, e))
                    self.__addReport(key, (val, ERR_FIELD_VALUE, "value type error"))
                    return

                if self.has_key(key):
                    if val == self[key]:
                        return val

                if hasattr(self, "__fieldConvert__"):
                    key = self.__fieldConvert__.get(key, key)

                return super(BaseContainer, self).__setitem__(key, val)
            else:
                if self.__warnConvertFail__:
                    log.error("Unknown field - %s : %s" % (key, val))

                #self.__validityReport[key] = [val, ERR_UNKNOWN_FIELD, "unknown field"]

        def update(self, *args, **kwargs):
            for key, val in dict(*args, **kwargs).iteritems():
                self[key] = val

            if self.__makeReport__:
                fieldList = filter(lambda x: not str(x).startswith('__'), dir(self.__fieldFormat__))
                for field in fieldList:
                    attr = getattr(self.__fieldFormat__, field)
                    if not isinstance(attr, dict):
                        continue

                    # 필수 필드인데, 로그에 없는 경우
                    if not attr.get('opt', 0) and not self.has_key(field):
                        self.__addReport(field, (None, ERR_FIELD_NOT_FOUND, "mandatory field not found"))
                        continue

                    # 상위 필드의존성 체크
                    if attr.has_key('dep') and self.has_key(field):
                        for name, cond in attr['dep'].iteritems():
                            msg = self.__getValidityMessage(cond, self.get(name), name)
                            if msg:
                                self.__addReport(field, (self[field], ERR_DEPENDENCY, msg))
                """
                print "=" * 20
                for k, v in self.__validityReport.iteritems():
                    print k, v
                print "=" * 20
                """

                if len(self.__validityReport) > 0:
                    super(BaseContainer, self).__setitem__("__validityReport", self.__validityReport.copy())

    if callable(param):
        # No Params
        BaseContainer.__fieldFormat__ = param
        BaseContainer.__warnConvertFail__ = warn
        BaseContainer.__makeReport__ = report
        return BaseContainer
    else:
        # with params
        def generateContainer(cls):
            BaseContainer.__fieldFormat__ = cls
            if param:
                BaseContainer.__fieldConvert__ = param
            BaseContainer.__warnConvertFail__ = warn
            BaseContainer.__makeReport__ = report
            return BaseContainer
        return generateContainer
