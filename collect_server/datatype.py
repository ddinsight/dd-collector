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


import datetime
import re
import json

"""
Base Type
"""
IntegerType = int
LongType = long


class FloatType(object):
    def __new__(cls, val):
        if isinstance(val, (str, unicode)):
            val = val.replace(',', '.')
        return float(val)


class StringType(object):
    def __new__(cls, val):
        return str(val)

    class lower(object):
        def __new__(cls, val):
            return str.lower(str(val))


class UnicodeType(object):
    def __new__(cls, val):
        return unicode(val)

    class lower(object):
        def __new__(cls, val):
            return unicode.lower(unicode(val))


class BooleanType(object):
    """
    Convert boolean string to boolean value
    """
    def __new__(cls, val):
        if isinstance(val, bool):
            return val

        if str(val).lower() in ('1', 'true', 'active', 'on'):
            return 1
        else:
            return 0


class RawType(object):
    def __new__(cls, val):
        return val


class PhoneNumberType(object):
    def __new__(cls, val):
        _pattPhone = '^\+?([0-9]{12})$|^([0-9]{11})$|^([0-9]{10})$'
        if re.match(_pattPhone, val):
            return val.lstrip('+')


class IPv4Type(object):
    def __new__(cls, val):
        tmpVal = (256, 255, 255, 255)
        try:
            nums = map(int, val.split('.'))

            #for IPv4
            if nums[0] < 0:
                for i in range(0, 4):
                    nums[i] = tmpVal[i] + nums[i]

            return ".".join(map(str, nums))
        except Exception, e:
            raise ValueError("IPAddress : Unknown format - %s" % val)

        return val


class EthAddrType(object):
    @staticmethod
    def __check(val):
        _pattEthHwAddr = '^([a-fA-F0-9]{2}:){5}[a-fA-F0-9]{2}$'
        if re.match(_pattEthHwAddr, val):
            return val
        raise ValueError("EthernetAddr - Invalid format : %s" % val)

    def __new__(cls, val):
        return cls.__check(val)

    @staticmethod
    def toLong(val):
        ret = EthAddrType.__check(val)
        return int(ret.replace(':', ''), 16)

    @staticmethod
    def fromLong(val):
        if not isinstance(val, (int, long)):
            raise ValueError("Cannot convert val to eth addr : " + val)

        tmp = hex(val).strip('0x*L').zfill(12)
        ethAddr = ':'.join(re.compile(r'.{2}').findall(tmp))
        return ethAddr


class DatetimeType(object):
    """
    convert date string to datetime.datetime value
    """
    def __new__(cls, val):
        if isinstance(val, datetime.datetime):
            return val

        if re.match("^[\d]{4}-(\d+)-(\d+) ((\d+):){2}(\d+)", val):
            return datetime.datetime.strptime(str(val).strip(), "%Y-%m-%d %H:%M:%S")

        raise ValueError("Datetime : Unknown format - %s" % val)


class TimestampType(object):
    """
    convert date string to datetime.datetime value
    """
    def __new__(cls, val):
        if isinstance(val, (float, int)) or re.match("^(\d+).?(\d+)$",val):
            return int(val)

        raise ValueError("Timestamp : Unknown format - %s" % val)


class JSONType(object):
    def __new__(cls, val):
        if isinstance(val, dict):
            return val
        elif isinstance(val, (str, unicode)):
            return json.loads(val)
