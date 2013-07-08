import sys, os
import re

BASE_PATH="/home/mtoth/skola/dp/hadoop-common"
#LOG_FILE="/home/mtoth/Desktop/logy/hadoop-all-prod-log-export.txt"
LOG_FILE="/home/mtoth/Desktop/logy/hadoop-rewritten-logs"

LINES=14


#
# /home/mtoth/skola/dp/hadoop-common
#                     ./hadoop-tools/hadoop-archives/src/main/java/org/apache/hadoop/tools/HadoopArchives.java
# /home/mtoth/skola/dp/hadoop-common/hadoop-archives/src/main/java/org/apache/hadoop/tools/HadoopArchives.java
#


# Insert Logger declaration and definition into java file
def get_first_method():
    # TODO
    None

# Find position in java file for substitution with new log
def find_current_log():
    None

def parse_package(line):    
    package = line[0:line.index(" ")]
    #print "package=", package
    return package, search_basedir(package)
    # return package

def parse_namespace(line):    
    namespace = "src/main/java/" + line[0:line.index(" ")].replace(".", "/")
    print "namespace=", namespace

    return namespace

def parse_java_file(line):
    java_file = line[0:line.index(" ")]
    print "java_file=", java_file
    return java_file

def parse_log(line):
    #print "log=", line
    return line


# search for package (directory) in BASE_PATH
def search_basedir(package_name):

    #package_name = "hadoop-yarn-server-resourcemanager"

    # for name in os.listdir(BASE_PATH):
    #     path = BASE_PATH + "/" + name
    #     if os.path.isdir(path):      
    #         print path      
    #         for file_name in os.listdir(path):
    #         # search for files with similar/same name as package_name
    #             print "files=" + file_name
    #             if package_name == file_name:
    #                 print package_name, 'FOUND! Here -', path,  file_name
    #                 path = path + "/" + file_name
    #                 return path
                    

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



def search_all_packages():
    fr = open(LOG_FILE, 'r')
    
    l_number = 0    
    previous_line = None
    packages = []

    for line in fr:
        parsed_line = ""        
        if line == '':
            break;
        space_counter = 0
        found_first_letter = False;
        for letter in line:
            if letter.isspace() and not found_first_letter:
                space_counter = space_counter + 1
            else:
                found_first_letter = True;
                parsed_line += letter #sys.stdout.write(letter)

        parsed_line = parsed_line[:-1]  # remove last \n

        if space_counter == 12:
            package = parse_package(parsed_line)
            packages.append(package)



    print packages
    for name, path in packages:  
        if path == None: 
            print "package=", name, path


# TODO - finish
search_all_packages()


# Create path from custom log file
def parse_log_file():
    # spaces_function = {             
    #         0 : "0 spaces",
    #         4 : "production/testing",            
    #         8 : "un/classified usage", 
    #         12 : parse_package(),
    #         16 : parse_namespace(),
    #         20 : parse_java_file(),
    #         24 : parse_log()
    #         }
    fr = open(LOG_FILE, 'r')
    
    l_number = 0    
    previous_line = None
    while (l_number != LINES):
        parsed_line = ""
        line = fr.readline()
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
            package = parse_package(parsed_line)
        if space_counter == 16:
            namespace = parse_namespace(parsed_line)
        if space_counter == 20:
            java_file = parse_java_file(parsed_line)

        # if previous parsed_line = None or " " call with isOld = True                       
        if space_counter == 24:
            if previous_line.startswith('('):
                print 'new'
                new_log = parse_log(parsed_line)
            else:
                old_log = parse_log(parsed_line)



        previous_line = parsed_line
        l_number = l_number + 1

        
    full_path = BASE_PATH +"/" + package + "/" + namespace + "/" + java_file
    print full_path


#MAIN LOOP
#parse_log_file()





# Remove test classes and logs from log-file exported from idea
def remove_test_classes(filepath):
    fr = open(filepath, 'r')
    
    lines = []
    l_number = 1             
    remove_mode = False; 
    remove_list = []

    for line in fr:
        parsed_line = ""
        if line == '':
            break;
        space_counter = 0
        found_first_letter = False;
        for letter in line:
            if letter.isspace() and not found_first_letter:
                space_counter = space_counter + 1
            else:
                found_first_letter = True;
                parsed_line += letter #sys.stdout.write(letter)

        parsed_line = parsed_line[:-1]  # remove last \n

        if space_counter == 16:
            if remove_mode:
                remove_stop_line = l_number
                remove_mode = False
                remove_list.append((remove_start_line, remove_stop_line))
            if parsed_line.lower().find("test") != -1:
                remove_mode = True
                remove_start_line = l_number

        l_number = l_number + 1
        lines.append((l_number, line))
    
    print remove_list
    fr.close()    

    print "number of lines=", len(lines)
    

    fr = open(filepath + "-no-tests", 'w')

    to_remove = []
    for removal in remove_list:
        for i in range(removal[0], removal[1]):
            print i
            to_remove.append(i)


    for i, line in lines:
        if i not in to_remove:
            fr.write(line)  
            
file_name = "/home/mtoth/Desktop/hadoop-new-logs.txt"
#remove_test_classes(file_name)


def print_spaces(number):
    spacing = ""
    for i in range(0, number):
        spacing += " " 
    return spacing


# Merge OLD and NEW "output-log-file" from idea log search export.
# Also insert new logs to appropriate position. (DOES NOT DO UPDATE!!)
def merge_log_files(file_old, file_new):
    fr = open(file_old, "r") 
    fnew = open(file_new, "rw")
    f_write = open("hadoop-rewritten-logs", "r+")

    l_number = 0    
    previous_line = None
    new_log = ""
    old_log = ""
    logs_to_change = []
    for line in fr:  
        parsed_line = ''
        space_counter = 0
        found_first_letter = False;
        for letter in line:
            if letter.isspace() and not found_first_letter:
                space_counter = space_counter + 1
            else:
                found_first_letter = True;
                parsed_line += letter #sys.stdout.write(letter)

        parsed_line = parsed_line[:-1]  # remove last \n
        
        # if previous parsed_line = None or " " call with isOld = True                       
        if space_counter == 24:            
            if previous_line.startswith('(') and not parsed_line.startswith('('):
                new_log = parse_log(parsed_line)
            else:
                old_log = parse_log(parsed_line)

            
            if old_log != "" and new_log != "":
                # print old_log + "  --  " + new_log
                logs_to_change.append( (old_log.strip(), new_log.strip()) )
                old_log = ""
                new_log = ""
            if "LOG.USING_PLUGIN_JARS(Iterables.toString(jars)" in new_log:
                break
           
        previous_line = parsed_line
        l_number = l_number + 1

    fr.close()


    
    # for old, new in logs_to_change:
    #     print old, "\n", new, "\n"

    logs = logs_to_change

    spaces = print_spaces(24)
    i = 0
    changed = 0
    for line in fnew:
        newline = line
        line = line.lstrip()

        for old, new in logs:#_to_change:
            if old[old.lower().find(') '):45] in line:

                if old.strip() != line.strip():
                    #print old, line
                    pass
        #         print 'old=', old[old.lower().find('log'):30].strip() , '\nin=', line
                newline = str.replace(line, line, spaces + line + spaces + new.rstrip() + "\n\n")            
                print "old=" + old, "\n", line, "new=\n", newline#, changed
        #         changed = changed + 1
                logs_to_change.remove((old, new))                
        #         break
                i = i + 1
        #f_write.write(newline)

    print "not swapped= ", i#len(logs_to_change)
    # for old, new in logs_to_change:
    #     print old
    #     print new
    #     print "\n"

#merge_log_files("/home/mtoth/Desktop/logy/hadoop-all-prod-log-export.txt", "hadoop-new-logs.txt-no-tests")





#log = '(1747: 9) LOG.warn("Unknown rpc kind "  + header.getRpcKind() + '
# dead code
def change_line(line):

    print line[line.lower().find('log'):]


    print "Line to change=", line.lower()
    line = line.lower()
    pattern = r"log.*"
    match = re.search(pattern, line)
    if match:
        print match.group(0)
        print match.start(0)

    # print "changed to=", newline
    # return newline

#change_line(log)