import sys
import os
import re

BASE_PATH="/home/mtoth/skola/dp/hadoop-common"
#LOG_FILE="/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-all-prod-log-export.txt"
LOG_FILE="/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-rewritten-logs"

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


# Insert Logger declaration and definition into java file
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
        return  parse_position(line)
    else:
        return line

def parse_position(line):
    positions_string = line[1:line.find(")")]
    pos = [int(x) for x in positions_string.split(": ")]
    log = line[line.find(")") + 2:]
    return log, pos

# Dictionary to substitute names and other not-automatically processable paths
names = {   'hadoop-main'               : 'hadoop-common-project', 
            'hadoop-hdfs-bkjournal'     : 'hadoop-hdfs'  }

# search for package (directory) in BASE_PATH
def search_basedir(package_name):             
    if package_name in names.keys():
        #print "changing '" + package_name + "' to '" + names[package_name] +"'"
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

# TODO
def is_generated_log(log):
    "LOG..tag().LEVEL();"
    return False

# TODO
def change_java_file(logChanger):
    print logChanger



# Create path from custom log file
def parse_log_file():
    # spaces_function = {             
    #         0 : "0 spaces",
    #         4 : "production/testing",            
    #         8 : "un/classified usage", 
    #         12 : parse_package(),
    #         16 : parse_namespace(),
    #         20 : parse_java_file(),
    #         28 : parse_log()
    #         }
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
                setattr(log, 'file_path', full_path)         
            
        # if previous parsed_line = None or " " call with isOld = True                       
        if space_counter == 28:
            if previous_line.startswith('(') and is_my_log(parsed_line):
                log.old_log, log.log_position = parse_log(previous_line)                 
                log.new_log = parse_log(parsed_line)

                if is_generated_log(log.new_log):
                # pre-autogenerated format "LOG..tag().LEVEL();":
                    pass
                else:   
                #   CALL CHANGING FUNCTION IN REAL LOGS 
                    change_java_file(log)       
           
        previous_line = parsed_line
        l_number = l_number + 1
    

    # Got full path to file -> search for LOG in JAVA file now.


#MAIN LOOP
parse_log_file()
