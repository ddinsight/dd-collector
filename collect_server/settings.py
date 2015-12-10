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


import json
import ConfigParser
from pattern import Singleton

DEFAULT_CONFIG_FILE = "config.local"


class ConfigHandler(ConfigParser.ConfigParser, object):
    __metaclass__ = Singleton

    def __init__(self, filename=DEFAULT_CONFIG_FILE):
        super(ConfigHandler, self).__init__()
        self.optionxform = str
        self.readFromFile(filename)

    def readFromFile(self, filename):
        try:
            fp = open(filename)
        except Exception, e:
            raise ValueError("Cannot open config file - %s" % filename)

        self.readfp(fp, filename = filename)
        fp.close()

    def update(self, options={}, keepOrgValue=False):
        """
        update configuration

        :param options: dict data of section, key-value
        :param keepOrgValue: if True, do not overwrite original value with new one.
        :return: None
        """
        for s, l in options.iteritems():
            if not isinstance(l, dict):
                self._add('misc', s, l)
            else:
                for k, v in l.iteritems():
                    if not keepOrgValue or not self.has_option(s, k):
                        self._add(s, k, v)

    def _add(self, section, option, value):
        if not self.has_section(section):
            self.add_section(section)
        (option, value) = map(lambda x:unicode(x).encode('utf-8'), (option, value))
        self.set(section, option, value)

    def dumps(self):
        config = {}
        for s in self.sections():
            config[s] = {}
            for k, v in self.items(s):
                config[s][k] = v

        return json.dumps(config, indent=4, sort_keys=True)
