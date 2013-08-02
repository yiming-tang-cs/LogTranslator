import os
import re

BASE_PATH = "/home/mtoth/skola/dp/hadoop-common"
#LOG_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-all-prod-log-export.txt"
LOG_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-rewritten-logs-short"
# LOG_FILE = "/home/mtoth/Desktop/hadoop-fresh-logs.txt"
NEW_FILE = "/home/mtoth/skola/dp/LogFilterBase/Log-change/logs-generated"

APPLICATION_NAMESPACE = "org.apache.hadoop"
# if given log has no/empty message, use this one
EMPTY_LOG_MESSAGE = "LOG"
LOG_START = "LOG."
# variable for limiting depth of namespace (org.apache.hadoop = 2 - "number of dots")
MAXIMUM_MODULE_DEPTH = 6

CLASS_TEMPLATE = [                            
                "import cz.muni.fi.logger.LoggerFactory;",
                "SampleNamespace LOG = LoggerFactory.getLogger(SampleNamespace.class);",
                'LOG.event1("abc", 123).tag("EntityA").tag("EntityB").log();'
                ]


NAMESPACE_CLASS_TEMPLATE = """ 
package cz.muni.fi.sampleproject;

import cz.muni.fi.annotation.Namespace;
import cz.muni.fi.logger.AbstractNamespace;

@Namespace
public class SampleNamespace extends AbstractNamespace {

    public AbstractNamespace event1(String param1, int param2) {
        return log(param1, param2);
    }

    public AbstractNamespace event2(double a, double b, boolean c) {
        return log(a, b, c);
    }

    public AbstractNamespace event3(double a, String ac) {
        return log(a, ac);
    }
}
"""

NAMESPACE_JAVA_CLASS_TEMPLATE = """ 
package cz.muni.fi.sampleproject;

import cz.muni.fi.annotation.Namespace;
import cz.muni.fi.logger.AbstractNamespace;

@Namespace
public class %s extends AbstractNamespace {
    %s
}
""" 




# Dictionary containing associated files to namespace.  
# <NAMESPACE_FILE> : <LOG_FILE_1, LOG_FILE_2, ... LOG_FILE_N>
modified_java_files = {}




class LogChanger(object):
    def __init__(self): #, file_path, old_log, new_log, position):
        self.data = []
        self.file_path = None
        self.old_log = None
        self.new_log = None
        self.position = None # (Log-start-line, Log-start-column, Log-end-line)
        self.namespace = None
        self.variables = []

    def __str__(self):
        return "LogChanger={\nPATH=" + str(self.file_path) + "\nPOS=" + \
        str(self.position) + "\nOLD=" + str(self.old_log) + \
        "\nNEW=" +  str(self.new_log) + "\nNS=" + str(self.namespace) + \
        "\nVARS=" + str(self.variables) +"}"


# Insert logger declaration and definition into java file
# def get_first_method():
#     None


def fetch_log(log_changer):
    """ Get complete log from java file from given part of log. """
    f = open(log_changer.file_path, "r+b")
    java_file = list(enumerate(f))        
    log = ""       
    # Log is not finished
    for i, line in java_file[log_changer.position[0]-1:]:
        log = log + line.strip() + " "
        if ";" in line:
            # if line is not COMMENTED - break! else ignore line
            if "//" not in line:
                log_changer.position.append(i+1)
                break
    log_changer.old_log = log
    f.close()
    return log_changer


# TODO!! Use ANTLR??
def fetch_variables_type(log):
    """ Method recognized basic types of variables 
        byte, short, int, long, float|double, boolean, String.
        If it fails to find type, fallback to String type.
    """    
    
    f = open(log.file_path, "r+b")
    java_file = list(enumerate(f))        

    #print log.file_path, log.position, log.variables
    # for i, line in java_file:
    #     print i, line.strip()

    variables = []
    # fall back mode:
    for v in log.variables:
        variables.append((v, "String"))
    log.variables = variables

    return log


def insert_log_to_java_file(log_changer):
    # If in this file for 1st time, declare & define JSONLogger and Namespace 
    # Insert into java_file on appropriate position LOG
    global modified_java_files                
    
    #local_directory = log_changer.file_path[:log_changer.file_path.rfind(os.sep)+1]
    #print local_directory

    if log_changer.namespace not in modified_java_files.keys():
        # Create new Namespace.java file with methods from actual log  
        modified_java_files[log_changer.namespace] = []

        i = 0        
        variable_list = []
        for v in log_changer.variables:
            variable = v[0]
            if "()" in variable:
                variable = variable.replace(".", "_").replace("()", "")
            variable_list.append(variable)
            if i == 0:                
                parameters = v[1] + " " + variable    
                i = i + 1
            else:
                parameters = parameters + ", " + v[1] + " " + variable        

        method = "public AbstractNamespace %s(%s) {\n\t\treturn log(%s);\n\t}" \
                % (log_changer.new_log[4:log_changer.new_log.find("(")],
                 parameters, ", ".join(variable_list))

        new_file = NAMESPACE_JAVA_CLASS_TEMPLATE % (log_changer.namespace, method)
        modified_java_files[log_changer.namespace].append(log_changer.file_path)

        #print new_file, modified_java_files

    if log_changer.file_path not in modified_java_files: #does not work yet
        # Update moduleNamespace file with new logs.
        pass
    #if log_changer.file_path not in modified_java_files[log_changer.namespace]:
        # DECLARE LOG & NAMESPACE        
        # go to appropriate line and change log in it
        f = open(log_changer.file_path, "rw+b")
        java_file = list(enumerate(f))        
        pass

    f.close()


def parse_package(line):    
    package = line[0:line.index(" ")]
    return search_basedir(package)


def parse_namespace(line): 
    if line.startswith("/"): # windows paths not covered yet  
        namespace = line[:line.find("(") - 2]        
    else:
        #namespace = "src" + os.sep + "main" + os.sep + "java" + os.sep + line[0:line.index(" ")].replace(".", os.sep)
        namespace = os.path.join("src", "main", "java", line[0:line.index(" ")].replace(".", os.sep))
    return namespace


def parse_java_file(line):
    java_file = line[0:line.index(" ")]
    return java_file

def parse_log_and_position(line):
    """ Method returns parsed log and position """
    if line.startswith('('):
        positions_string = line[1:line.find(")")]
        try:
            pos = [int(x) for x in positions_string.split(": ")]
            log = line[line.find(")") + 2:]
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

    for root, dirnames, files in os.walk(BASE_PATH):          
        if package_name in dirnames: 
            path = root + os.sep + package_name                                    
            return package_name, path                    
    # if BASE_PATH in path:
    #     stripped_path = path[len(BASE_PATH):]
    # return path + "/" + package_name


def findnth(haystack, nth):
    parts = haystack.split(".", nth+1)
    return len(haystack)-len(parts[-1])-1


def parse_message(line):
    """  Too simple message parsing. Implement better message mechanism. """
    message = ""
    if (line.count('"') == 2) and (line.find("{") == -1):
        message = line[line.find('"')+1:line.rfind('"')]
        # message = message.replace("-", "_").upper()
        #message = message.replace(" ", "_").upper()            
        # remove all but alfa-num characters                        
        message = re.sub(r'[\W]+', "_", message.strip().upper())
        # remove multi-underscores        
        #message = re.sub(r'_+', "_", message)             

        #if message.startswith('_'):
        #    message = message[1:] 
        #if message.endswith('_'):  
        #    message = message[:-1]         
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

    level = logold[4:logold.find("(")] + "();"    
    # LOG.MESSAGE
    message = parse_message(logold)   
    # LOG.MESSAGE(VARIABLE[S])
    end_pos = logold.rfind(")")
    if logold[end_pos-1] == "(":
        # fix for ending object.method()\EOL
        end_pos = end_pos + 1    

    variables = parse_variables(logold[logold.find("(")+1:end_pos])     
    generated_log = LOG_START + message + "(" + ', '.join(map(str, variables)) + ').tag("' + log_changer.namespace + '").' + level    
    #print logold, "\n", generated_log + "\n"
    return generated_log, variables


def handle_log_line(log, previous_line, parsed_line):               
    """ Handle log on given line. Generate new log (if needed) and insert it into java file."""
    if previous_line.startswith('(') and parsed_line.startswith('LOG.'):        
        if parsed_line.endswith(";"):           
            # Log is finished and ready to be inserted into java file  
            log.new_log = parsed_line            

    elif parsed_line.startswith("("):
        # No log has been generated for this line
        log.old_log, log.position = parse_log_and_position(parsed_line)
        if not parsed_line.endswith(";"):
            #log has to be fetched & finished from java file                      
            log = fetch_log(log)                        
        log.new_log, log.variables = generate_log(log)
    else:
        print "ERROR! Some bullshit is on the line, ignore it... or?? =", parsed_line    
    
    return fetch_variables_type(log)
    #print parsed_line, log


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
                full_path =  package[1] + os.sep + namespace
            else:
                full_path = namespace

        if space_counter == 20:
            java_file = parse_java_file(parsed_line) 

            if full_path.endswith('.java'):
                # remove java-file & add new file.java
                full_path = full_path[:full_path.rfind(os.sep) + 1] + java_file
            else:
                full_path = full_path + os.sep + java_file    
            
            log.file_path = full_path
            # parse namespace from full_path
            namespace = APPLICATION_NAMESPACE.replace(os.sep, ".")
            path = full_path.replace(os.sep, ".")
            module = path[path.find(namespace):path.rfind(".")]
            module = module[:module.rfind(".")]
            module = module[:findnth(module, MAXIMUM_MODULE_DEPTH)]
            
            log.namespace = module
            
        if space_counter == 28:  
            log = handle_log_line(log, previous_line, parsed_line)
            
            # print "previous=" + previous_line + "\nparsed=" + parsed_line + "\nold=" + log.old_log + "\nnew=" + log.new_log + "\n"#, log.file_path + "\n\n"
            print log.old_log + "\n" + log.new_log + "\n"#, log.file_path + "\n\n"
            # CALL CHANGING FUNCTION IN REAL LOGS 
            insert_log_to_java_file(log)

        #write_to_file()
        previous_line = parsed_line
        l_number = l_number + 1
    

#MAIN LOOP
parse_log_file()