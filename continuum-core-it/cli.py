import os
import sys
import string

class cli:
    prompt = '# '

    def __init__(self):
        try:
            import readline
            readline.set_completer(self.complete)
            readline.parse_and_bind("tab: complete")
        except:
            pass

    def cmdloop(self):
        stop = None
        while not stop:
            try:
                line = raw_input(self.prompt)
            except EOFError:
                print
                break

            line = self.precmd(line)
            stop = self.onecmd(line)
            stop = self.postcmd(stop, line)

        self.postloop()

    def postloop(self):
        pass

    def precmd(self, line):
        return line

    def postcmd(self, stop, line):
        return stop

    def onecmd(self, line):
        line = string.strip(line)
        if not line:
            return None
        tokens = string.split(line)

        help = 0
        if tokens[0] == 'help' or tokens[0] == 'man':
            help = 1
            del(tokens[0])

        func = None
        for i in range(len(tokens), 0, -1):
            try:
                func = getattr(self, 'do_' + string.join(tokens[:i], '_'))
                args = tokens[i:]
            except AttributeError:
                func = None
            else:
                break

        if help:
            self.do_help(func)
        elif func:
            return func(args)
        else:
            print "%s: no such command" % (tokens[0])
            return None

    def do_help(self, func):
        """Welcome to the help system"""
        if func:
            if func.__doc__:
                print func.__doc__
            else:
                print "No help available"
        else:
            sortedkeys = self.__class__.__dict__.keys()
            sortedkeys.sort()
            for i in sortedkeys:
                if i[0:3] == 'do_':
                    print "  %-20s  %s" % \
                      ( string.replace(i[3:], '_', ' '),
                        string.split(self.__class__.__dict__[i].__doc__, "\n")[0])

        return None
        
    def complete(self, text, state):
        #import readline
        #print
        #print "DEBUG bidx =", readline.get_begidx()
        #print "DEBUG eidx =", readline.get_endidx()
        #print "DEBUG line =", readline.get_line_buffer()
        #print "DEBUG text =", text
        if state == 0:
            self.matches = self.global_matches(text)
        try:
            return self.matches[state]
        except IndexError:
            return None

    def global_matches(self, text):
        matches = []
        n = len(text)

        for word in self.__class__.__dict__.keys():
            if word[0:3] == 'do_':
                word = string.join(string.split(word, '_')[1:], ' ')
                if word[:n] == text:
                    matches.append(word)

        return matches

