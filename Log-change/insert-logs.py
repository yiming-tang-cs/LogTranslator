import sys
import os
import re

BASE_PATH="/home/mtoth/skola/dp/hadoop-common"
#LOG_FILE="/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-all-prod-log-export.txt"
LOG_FILE="/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-rewritten-logs"#-short"
NEW_FILE="/home/mtoth/skola/dp/LogFilterBase/Log-change/logs-generated"

APPLICATION_NAMESPACE="org.apache.hadoop"

# variable for limiting depth of namespace (org.apache.hadoop = 2 - "number of dots")
MAXIMUM_MODULE_DEPTH=6
#
# /home/mtoth/skola/dp/hadoop-common
#                     ./hadoop-tools/hadoop-archives/src/main/java/org/apache/hadoop/tools/HadoopArchives.java
# /home/mtoth/skola/dp/hadoop-common/hadoop-archives/src/main/java/org/apache/hadoop/tools/HadoopArchives.java
#

class LogChanger:
    def __init__(self): #, file_path, old_log, new_log, log_position):
        self.data = []
        self.file_path = None
        self.old_log = None
        self.new_log = None
        self.log_position = None

    def __str__(self):
        return "LogChanger={\n" + self.file_path + "\n" + str(self.log_position) + "\nOLD=" + self.old_log + "\nNEW=" +  self.new_log + "\n}"



# Insert logger declaration and definition into java file
def get_first_method():
    # TODO
    None

# Find position in java file for substitution with new log
def find_current_log():
    # TODO
    None

def parse_package(line):    
    package = line[0:line.index(" ")]
    return search_basedir(package)

def parse_namespace(line): 
    if line.startswith("/"): 
        namespace = line[:line.find("(") - 2]        
    else:
        namespace = "src/main/java/" + line[0:line.index(" ")].replace(".", "/")
    return namespace

def parse_java_file(line):
    java_file = line[0:line.index(" ")]
    return java_file

def parse_log(line):
    if line.startswith('('):
        return parse_position(line)
    else:
        return line

def parse_position(line):
    positions_string = line[1:line.find(")")]
    try:
        pos = [int(x) for x in positions_string.split(": ")]
        log = line[line.find(")") + 2:]
        return log.strip(), pos
    except:
        print "Error parsing position in line" + line
        raise

# Dictionary to substitute names and other not-automatically processable paths
names = {   'hadoop-main'               : 'hadoop-common-project', 
            'hadoop-hdfs-bkjournal'     : 'hadoop-hdfs'  }

# search for package (directory) in BASE_PATH
def search_basedir(package_name):          
    if package_name in names.keys():
        package_name = names[package_name]

    found = False
    for root, dirnames, files in os.walk(BASE_PATH):        
        #print dirs      
        if package_name in dirnames:
            #print "Here I am :-) ", root, package_name 
            path = root + "/" + package_name                                    
            return package_name, path
            break
    # if BASE_PATH in path:
    #     stripped_path = path[len(BASE_PATH):]
    # return path + "/" + package_name


def is_my_log(line):
    # "LOG.SOMETHING_INTERESTING("
    pattern = re.compile(r'^LOG\.[A-Z_]+\(*')
    return pattern.match(line.strip())

# def is_generated_log(log):
#     "LOG..tag().LEVEL();"
#     return False

def findnth(haystack, n):
    parts = haystack.split(".", n+1)
    return len(haystack)-len(parts[-1])-1

# Too simple
def parse_message(line):
    message = ""
    if (line.count('"') == 2) and (line.find("{") == -1):
            message = line[line.find('"')+1:line.rfind('"')].strip()
            # message = message.replace("-", "_").upper()
            message = message.replace(" ", "_").upper()            

            #remove all but alfa-num characters                        
            message = re.sub(r'[\W]+', "", message).strip()
            message = message.replace("__", "_")
            if message.startswith('_'): 
                message = message[1:]                
    return message

def get_all_pluses(line):
    pluses = line.count("+")
    if pluses > 0:
        plus_positions = []
        quote_positions = []
        pos = 0
        for c in line:
            if c == "+":
                plus_positions.append(pos)
            if c == "\"":
                quote_positions.append(pos)
            pos = pos + 1
        return plus_positions, quote_positions
    else:
        return None
# TODO
def parse_variables(line):
    variables = []
    variable = ""

    print line

    plus_count = line.count("+")
    if line.find("\"") == -1:    
        variable = line.strip()

    elif plus_count != 0:
    # there is at least one variable and text (which we ignore)
        plus_pos = line.find("+")                
        
        if plus_count == 1:
            # ""+var or var+""
            quote_pos = line.find("\"")
            if plus_pos < quote_pos:
                variable = line[:plus_pos]
            else:
                variable = line[plus_pos+1:]
            variable = variable.strip()
        
        if plus_count >= 2:
            # +var+
            pluses, quotes = get_all_pluses(line)
            #print line, pluses, quotes

            for i in range(0, len(pluses)-1):

                # if there is " between + + take borders  
                if "\"" in line[pluses[i]:pluses[i+1]]:
                    if i == 0:
                        variables.append(line[:pluses[i]].strip())   
                    elif i == len(pluses):
                        pass

                    else:
                        # take all on left
                        variables.append(line[pluses[i-1]+1:pluses[i]].strip())
                        #print variables, line

                        # take on right until next + or stop
                    #     if i == len(pluses)-1:
                    #         #print i
                    #         variables.append(line[pluses[i]:pluses[i+1]])
                    #     # else:
                    #     #     variables.append(line[pluses[i]:])
                    # i = i + 1
                else:
                    # variable is between
                    variables.append(line[pluses[i]+1:pluses[i+1]].strip())                    
                #print i, pluses, variables, line

            # for i in range(0, len(pluses)):
            #     if pluses[i] < quotes[i]:
            #         if i == 0:
            #             variables.append(line[:pluses[i]].strip())
            #         else:
            #             variables.append(line[pluses[i-1]:pluses[i]].strip())
            #     else:
            #         #get position of the closest quote-pair
            #         if i+2 in range(0, len(quotes)):
            #             if pluses[i] < quotes[i+2]:
            #                 pass



    if "," in variable:
        variables = [var.strip() for var in variable.split(",")]

    #print variable, line +"\n"
    if variables == []:
        variables.append(variable)

    #print variables, line
    return variables


# Simple Log generator. Checks for ";"
def generate_log(log_line, logChanger):    
    message = ""
    variables = []
    simple_log, pos = parse_position(log_line) 

    # parse namespace from full_path
    ns = APPLICATION_NAMESPACE.replace("/", ".")
    path = logChanger.file_path.replace("/", ".")
    module = path[path.find(ns):path.rfind(".")]
    module = module[:module.rfind(".")]
    module = module[:findnth(module, MAXIMUM_MODULE_DEPTH)]
    
    level = simple_log[4:simple_log.find("(")] + "();"


    if simple_log.endswith(";"):   
    # change only whole logs              
        # LOG.MESSAGE
        message = parse_message(simple_log)   


        # LOG.MESSAGE(VARIABLE)
        sl = simple_log
        variables = parse_variables(simple_log[sl.find("(")+1:sl.rfind(")")])

        
    else:
        # if long is not whole or trivial, we won't change much,
        # try to parse some messages at least 
        # except trivial "LOG..tag().LEVEL();"
        # print simple_log
        pass

    generated_log = 'LOG.' + message + "(" + ', '.join(map(str, variables)) + ').tag("' + module + '").' + level    
    #print simple_log, "\n", generated_log + "\n"
    return generated_log


# TODO
def change_java_file(logChanger):
    # If in this file for 1st time, declare JSONLogger and Namespace 
    # Insert into java_file on appropriate position LOG
    #print logChanger
    pass


# Create path from custom log file
def parse_log_file():
# 0 : "0 spaces",
# 4 : "production/testing",            
# 8 : "un/classified usage", 
# 12 : parse_package(),
# 16 : parse_namespace(),
# 20 : parse_java_file(),
# 28 : parse_log()
    fr = open(LOG_FILE, 'r')    
    l_number = 0    
    previous_line = None
    full_path = ''
    log = LogChanger()

    for line in fr:   
        parsed_line = ""     
        space_counter = 0
        found_first_letter = False;
        for letter in line:
            if letter.isspace() and not found_first_letter:
                space_counter = space_counter + 1
            else:
                found_first_letter = True;
                parsed_line += letter #sys.stdout.write(letter)

        parsed_line = parsed_line[:-1]  # remove last \n

        # which function to call 
        if space_counter == 12:
            full_path = "" 
            package = parse_package(parsed_line)
        if space_counter == 16:
            namespace = parse_namespace(parsed_line)
            if namespace.startswith("src"):
                full_path =  package[1] + "/" + namespace
            else:
                full_path = namespace
        if space_counter == 20:
            java_file = parse_java_file(parsed_line) 

            if full_path.endswith('.java'):
                # remove java-file & add new file.java
                full_path = full_path[:full_path.rfind("/") + 1] + java_file
            else:
                full_path = full_path + "/" + java_file    
                log.file_path = full_path #setattr(log, 'file_path', full_path)         
            
        if space_counter == 28:
            if previous_line.startswith('('):
                log.old_log, log.log_position = parse_log(previous_line)

                if is_my_log(parsed_line):
                    #
                    # this is my new log - insert it into appropriate FILE.java                                     
                    log.new_log = parse_log(parsed_line)
   

                else:
                    # generate simple log
                    log.new_log = generate_log(parsed_line, log)
                    #print log.new_log, parsed_line

                #   CALL CHANGING FUNCTION IN REAL LOGS 
                change_java_file(log)  

        #write_to_file()
        previous_line = parsed_line
        l_number = l_number + 1
    




#MAIN LOOP
parse_log_file()