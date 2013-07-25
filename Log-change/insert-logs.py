import os
import re

BASE_PATH = "/home/mtoth/skola/dp/hadoop-common"
#LOG_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-all-prod-log-export.txt"
LOG_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-rewritten-logs"#-short"
NEW_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/logs-generated"

APPLICATION_NAMESPACE = "org.apache.hadoop"
# if given log has no/empty message, use this one
EMPTY_LOG_MESSAGE = "LOG"
LOG_START = "LOG."
# variable for limiting depth of namespace (org.apache.hadoop = 2 - "number of dots")
MAXIMUM_MODULE_DEPTH = 6

# /home/mtoth/skola/dp/hadoop-common
#                     ./hadoop-tools/hadoop-archives/src/main/java/org/apache/hadoop/tools/HadoopArchives.java
# /home/mtoth/skola/dp/hadoop-common/hadoop-archives/src/main/java/org/apache/hadoop/tools/HadoopArchives.java

class LogChanger:
    def __init__(self): #, file_path, old_log, new_log, log_position):
        self.data = []
        self.file_path = None
        self.old_log = None
        self.new_log = None
        self.log_position = None

    def __str__(self):
        return "LogChanger={\n" + self.file_path + "\n" + \
        str(self.log_position) + "\nOLD=" + self.old_log + \
        "\nNEW=" +  self.new_log + "\n}"


# Insert logger declaration and definition into java file
def get_first_method():
    # TODO
    None

# Incomplete logs (not ending ";") 
def complete_java_log(simple_log, log_changer):
    #print simple_log, log_changer
    pass

# TODO
def insert_log_to_java_file(log_changer):
    # If in this file for 1st time, declare & define JSONLogger and Namespace 
    # Insert into java_file on appropriate position LOG
    #print log_changer
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
NAMES = {   'hadoop-main'               : 'hadoop-common-project', 
            'hadoop-hdfs-bkjournal'     : 'hadoop-hdfs'  }

# search for package (directory) in BASE_PATH
def search_basedir(package_name):          
    if package_name in NAMES.keys():
        package_name = NAMES[package_name]

    found = False
    for root, dirnames, files in os.walk(BASE_PATH):        
        #print dirs      
        if package_name in dirnames:
            #print "Here I am :-) ", root, package_name 
            path = root + "/" + package_name                                    
            return package_name, path                    
    # if BASE_PATH in path:
    #     stripped_path = path[len(BASE_PATH):]
    # return path + "/" + package_name


def is_my_log(line):
    # "LOG.SOMETHING_INTERESTING("
    pattern = re.compile(r'^LOG\.[A-Z_]+\(*')
    return pattern.match(line.strip())


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
    if message == "":
        message = EMPTY_LOG_MESSAGE               
    return message


def parse_variables(line):
    variables = []    
    pattern = r'(?:\"[^"]+\")*(?P<var>[^"]+)(?:\"[^"]+\")*|(?:\"[^"]+\")'
    
    found =  re.findall(pattern, line, re.IGNORECASE)
    if found != ['']:        
        for found_var in found: 
            if found_var.strip() != '':
                if "+" in found_var:
                    found_var = found_var.replace("+", "").strip()

                if "," in found_var:
                    variables = [v.strip() for v in found_var.split(",") if v.strip() != '']

                elif found_var != '':                    
                    variables.append(found_var.strip())
    # print line, "---->", variables
    return variables


# Simple Log generator. Checks for ";"
def generate_log(log_line, log_changer):    
    message = ""
    variables = []
    simple_log, pos = parse_position(log_line) 

    # parse namespace from full_path
    namespace = APPLICATION_NAMESPACE.replace("/", ".")
    path = log_changer.file_path.replace("/", ".")
    module = path[path.find(namespace):path.rfind(".")]
    module = module[:module.rfind(".")]
    module = module[:findnth(module, MAXIMUM_MODULE_DEPTH)]
    
    level = simple_log[4:simple_log.find("(")] + "();"
    # change only whole logs              
    # LOG.MESSAGE
    message = parse_message(simple_log)   
    # LOG.MESSAGE(VARIABLE)
    sl = simple_log
    variables = parse_variables(simple_log[sl.find("(")+1:sl.rfind(")")])
    # print simple_log, "===> ", message, variables  
    generated_log = LOG_START + message + "(" + ', '.join(map(str, variables)) + ').tag("' + module + '").' + level    
    #print simple_log, "\n", generated_log + "\n"
    return generated_log


# Create path from custom log file
def parse_log_file():
# 0 : "0 spaces",
# 4 : "production/testing",            
# 8 : "un/classified usage", 
# 12 : parse_package(),
# 16 : parse_namespace(),
# 20 : parse_java_file(),
# 24 : method name - NOT USED,
# 28 : parse_log()
    fr = open(LOG_FILE, 'r')    
    l_number = 0    
    previous_line = None
    full_path = ''
    log = LogChanger()    

    for line in fr:   
        parsed_line = ""     
        space_counter = 0
        found_first_letter = False
        for letter in line:
            if letter.isspace() and not found_first_letter:
                space_counter = space_counter + 1
            else:
                found_first_letter = True
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
            if previous_line.startswith('(') and parsed_line.startswith('LOG.'):
                print "complete log=" + parsed_line
                if parsed_line.endswith(";"):
                    # TODO & CHECK
                    insert_log_to_java_file(log)

            elif parsed_line.startswith("("):
                #print "pl (=", parsed_line
                log.old_log, log.log_position = parse_log(parsed_line)

                if is_my_log(parsed_line):
                    # this is my new log - insert it into appropriate FILE.java                                     
                    log.new_log = parse_log(parsed_line)
   
                else:
                    # generate simple log
                    
                    if not parsed_line.endswith(";"): 
                        complete_java_log(parsed_line, log) 
                        # if long is not whole or trivial, we won't change much,
                        # try to parse some messages at least, except trivial "LOG..tag().LEVEL();"
                        # Go to appropriate java file and fetch WHOLE LOG! Don't be lazy!!!
    
                    log.new_log = generate_log(parsed_line, log)
                    #print log.new_log, parsed_line

                # CALL CHANGING FUNCTION IN REAL LOGS 
                insert_log_to_java_file(log)  
            else:
                #print "ELSE??=", parsed_line
                pass

        #write_to_file()
        previous_line = parsed_line
        l_number = l_number + 1
    




#MAIN LOOP
parse_log_file()