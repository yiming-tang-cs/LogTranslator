import os
import re

BASE_PATH = "/home/mtoth/skola/dp/hadoop-common"
#LOG_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-all-prod-log-export.txt"
LOG_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-rewritten-logs"#-short"
# LOG_FILE = "/home/mtoth/Desktop/hadoop-fresh-logs.txt"
NEW_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/logs-generated"

APPLICATION_NAMESPACE = "org.apache.hadoop"
# if given log has no/empty message, use this one
EMPTY_LOG_MESSAGE = "LOG"
LOG_START = "LOG."
# variable for limiting depth of namespace (org.apache.hadoop = 2 - "number of dots")
MAXIMUM_MODULE_DEPTH = 6

class LogChanger:
    def __init__(self): #, file_path, old_log, new_log, log_position):
        self.data = []
        self.file_path = None
        self.old_log = None
        self.new_log = None
        self.log_position = None

    def __str__(self):
        return "LogChanger={\nPATH=" + str(self.file_path) + "\nPOS=" + \
        str(self.log_position) + "\nOLD=" + str(self.old_log) + \
        "\nNEW=" +  str(self.new_log) + "\n}"


# Insert logger declaration and definition into java file
# def get_first_method():
#     # TODO
#     None



def fetch_full_log(log_changer):
    """ Get complete logs from given part of log (not ending with ";")  """
    f = open(log_changer.file_path, "r+b")
    java_file = list(enumerate(f))        
    log = ""       

    for i, line in java_file[log_changer.log_position[0]-1:]:
        log = log + line.strip() + " "
        # if "AsyncDataService" in log_changer.file_path:
        #     print "line=", line, i, log_changer.log_position
        #     print 'log=', log
        if ";" in line:
            # if line is not COMMENTED - break! else ignore line
            if "//" not in line:
                break
    f.close()
    #print log, log_changer, "\n\n"
    return log


# TODO
def insert_log_to_java_file(log_changer):
    # If in this file for 1st time, declare & define JSONLogger and Namespace 
    # Insert into java_file on appropriate position LOG
    #print log_changer
    pass


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

# Method returns parsed log and position
def parse_log_and_position(line):
    if line.startswith('('):
        positions_string = line[1:line.find(")")]
        try:
            pos = [int(x) for x in positions_string.split(": ")]
            log = line[line.find(")") + 2:]
            #print log, line
            return log.strip(), pos
        except:
            print "Error parsing position in line" + line
            raise
    else:
        return line

# Dictionary to substitute names and other not-automatically processable paths
NAMES = {   'hadoop-main'               : 'hadoop-common-project', 
            'hadoop-hdfs-bkjournal'     : 'hadoop-hdfs'  }

def search_basedir(package_name):          
    """ Search for package (directory) in BASE_PATH """
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


def findnth(haystack, n):
    parts = haystack.split(".", n+1)
    return len(haystack)-len(parts[-1])-1


# TODO - implement better message mechanism.
def parse_message(line):
    """  Too simple + fix needed (see 1st log) """
    message = ""
    if (line.count('"') == 2) and (line.find("{") == -1):
        message = line[line.find('"')+1:line.rfind('"')].strip()
        # message = message.replace("-", "_").upper()
        message = message.replace(" ", "_").upper()            
        # remove all but alfa-num characters                        
        message = re.sub(r'[\W]+', "", message).strip()
        # remove multi-underscores        
        message = re.sub(r'_+', "_", message)             

        if message.startswith('_'):
            message = message[1:] 
        if message.endswith('_'):  
            message = message[:-1]         
        #print message  
    if message == "":
        #print "not (correctly) parsed=", line
        message = EMPTY_LOG_MESSAGE               
    return message



def parse_variables(line):
    """ Parse all variables from java log. Variables are separated by '+' or ','. """
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
    #print line, "---->", variables
    return variables


def generate_log(log_changer):
    """ Simple custom log generator. """    
    logold = log_changer.old_log
    message = ""
    variables = []

    # parse namespace from full_path
    namespace = APPLICATION_NAMESPACE.replace("/", ".")
    path = log_changer.file_path.replace("/", ".")
    module = path[path.find(namespace):path.rfind(".")]
    module = module[:module.rfind(".")]
    module = module[:findnth(module, MAXIMUM_MODULE_DEPTH)]
    
    level = logold[4:logold.find("(")] + "();"
    # change only whole logs              
    # LOG.MESSAGE
    message = parse_message(logold)   
    # LOG.MESSAGE(VARIABLE[S])
    variables = parse_variables(logold[logold.find("(")+1:logold.rfind(")")])     
    generated_log = LOG_START + message + "(" + ', '.join(map(str, variables)) + ').tag("' + module + '").' + level    
    #print logold, "\n", generated_log + "\n"
    return generated_log


def handle_log_line(log, previous_line, parsed_line):               
    """ Handle log on given line. Generate new log (if needed) and insert it into java file."""
    #print log, previous_line, parsed_line, '\n'
    if previous_line.startswith('(') and parsed_line.startswith('LOG.'):        
        if parsed_line.endswith(";"):           
            # Log is finished and ready to be inserted into java file  
            log.new_log = parsed_line
            insert_log_to_java_file(log)
            # print log   
            return log.old_log, log.new_log

    elif parsed_line.startswith("("):
        # No log has been generated for this line
        log.old_log, log.log_position = parse_log_and_position(parsed_line)

        if not parsed_line.endswith(";"):
            #log has to be fetched & finished from java file                        
            log.old_log = fetch_full_log(log)                        
        
        log.new_log = generate_log(log)
        insert_log_to_java_file(log)
        return log.old_log, log.new_log
    else:
        print "ERROR! Some bullshit is on the line, ignore it... or?? =", parsed_line    

    # print parsed_line, log


def parse_log_file():
    """ 
    Main function of Log Changer project.
    Parse IDEA's file, with findings of all logs, decide what operation
    should happen, based on spaces in current line.
    Create logChanger object with file path, old and new log and log 
    position in java file.

    Deciding number of spaces do function:
        0 : "0 spaces",
        4 : "production/testing", - NOT USED
        8 : "un/classified usage", - NOT USED
        12 : parse_package(),
        16 : parse_namespace(),
        20 : parse_java_file(),
        24 : method name, - NOT USED
        28 : handle_log_line()
    """

    fr = open(LOG_FILE, 'r')    
    l_number = 0    
    previous_line = None
    full_path = ''
    log = LogChanger()    

    for line in fr:   
        parsed_line = ""     
        space_counter = 0
        found_first_letter = False

        # If by some accident we got line with spaces only, skip it
        if line.strip() == "":
            continue
        for letter in line:
            if letter.isspace() and not found_first_letter:
                space_counter = space_counter + 1
            else:
                found_first_letter = True
                parsed_line += letter #sys.stdout.write(letter)

        if parsed_line.endswith("\n"):
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
            log.file_path = full_path
            
        if space_counter == 28:
            #print log, "\nprev=" + previous_line, "\nparsed=", parsed_line            
            old_line, new_line = handle_log_line(log, previous_line, parsed_line)
            
            print "parsed_line=" + parsed_line + "\nold_line=" + old_line + "\nnew_line=" + new_line + "\n"#, log.file_path + "\n\n"
            # CALL CHANGING FUNCTION IN REAL LOGS 
            # insert_log_to_java_file(log)

        #write_to_file()
        previous_line = parsed_line
        l_number = l_number + 1
    




#MAIN LOOP
parse_log_file()