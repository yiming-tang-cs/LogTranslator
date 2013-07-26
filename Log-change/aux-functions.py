# Auxiliary and test functions 
import re

BASE_PATH="/home/mtoth/skola/dp/hadoop-common"
LOG_FILE="/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-rewritten-logs-short"

# Merge OLD and NEW "output-log-file" from idea 11 to idea 12 log search export.
# Also insert new logs to appropriate position. (DOES NOT DO UPDATE!!)
def parse_log(line):
    #print "log=", line
    return line

def print_spaces(number):
    spacing = ""
    for i in range(0, number):
        spacing += " " 
    return spacing

def merge_log_files(file_old, file_new):
    fr = open(file_old, "r") 
    fnew = open(file_new, "rw")
    f_write = open("hadoop-rewritten-logs", "w")
    f_error = open("hadoop-rewritten-logs-errors", "w")

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
    #     print old, "\n", new, "\n\n"

    logs = logs_to_change
    spaces = print_spaces(28)
    i = 0
    changed = 0
    for line in fnew:
        newline = line
        line = line.lstrip()

        for old, new in logs:
            if old[old.lower().find(') '):45] in line:                
                newline = str.replace(line, line, spaces + line + spaces + new.rstrip() + "\n\n")            
                print "old=" + old, "\n", line, "new=\n", newline, changed
                changed = changed + 1
                logs_to_change.remove((old, new))                
                i = i + 1
        f_write.write(newline)
    f_write.close()

    print "Swapped= ", i, "out of", len(logs_to_change)
    f_error.write("Following logs were not matched, so they were not modified in new file. Add them manually.\n\n")
    for old, new in logs_to_change:        
        f_error.write(old+"\n")
        f_error.write(new+"\n\n")


# old input file with logs written, new output file w/o logs
#merge_log_files("/home/mtoth/skola/dp/LogFilterBase/Log-change/hadoop-all-prod-log-export.txt", "hadoop-new-logs-no-tests.txt")


# Remove test classes and logs from log-file exported from idea
def remove_test_classes(filepath):        
    lines = []
    l_number = 1             
    remove_mode = False; 
    remove_list = []

    fr = open(filepath, 'r')
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


# Move level of logs from front to back
# LOG.debug()...  -> LOG....debug();
def change_level():
    file_name = "/home/mtoth/Desktop/logy/hadoop-all-prod-log-export.txt"
    output_name = "output.log"
    f = list(enumerate(open(file_name, "rw+b")))
    out_file = open(output_name, "w")

    for i, line in f:
        line = line.rstrip()
        match = re.search('LOG\.(trace|debug|info|warn|error|fatal)\(\)', line)

        if match:       
            newline = line[:match.start()+3] + line[match.end():-1] + line[match.start()+3:match.end()] + ";"
            print line
            print 'newline=', newline
            line = newline
        out_file.write(line+"\n")

# Search all packages/modules in BASE_DIR
def search_all_modules():
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
            #print 'pckg=', package
            packages.append(package)

    #print packages
    for name, path in packages:  
        if path == None: 
            print "package=", name, path
        elif "common" in path:
            print name, path

def search_my_logs():
    pattern = re.compile(r'^LOG\.[A-Z_]+\(*')
    for line in open(LOG_FILE, "r"):
        line =  line.strip()
        match = pattern.match(line)
        if match:
            print match.group()

def regexp_search():
    l = [
     '"dsadasd" + test, sad + "dsad"',
     '"Failed to load state.", e, asd, " blabolasd."',
     '"Unknown child node with name: " + childNodeName',  # !!!!!!  string + string
     '"User " + user + " removed from activeUsers, currently: " + var2',
     '"update:" + " application=" + applicationId + " request=" + request',
     '"Test" + test.id() + " test 2=" + max.id().getStuff() +',
     'max.getId(), error',
     'blabol, bla2',
     '"ffd"',
     '"comment 1 +"'
    ]

    for s in l:
        #           string         variable(s)     string
        pattern = r'(?:\"[^"]+\")*(?P<var>[^"]+)(?:\"[^"]+\")*|(?:\"[^"]+\")'
        found =  re.findall(pattern, s, re.IGNORECASE)
        
        variables = []
        if found != ['']:        
            for found_var in found: 
                if found_var.strip() != '':
                    if "+" in found_var:
                        found_var = found_var.replace("+", "").strip()

                    if "," in found_var:
                        variables = [v.strip() for v in found_var.split(",") if v.strip() != '']

                    elif found_var != '':                    
                        variables.append(found_var.strip())
                   
        print found, "---->", variables

def is_my_log(line):
    # "LOG.SOMETHING_INTERESTING("
    pattern = re.compile(r'^LOG\.[A-Z_]+\(*')
    return pattern.match(line.strip())


def fetch_full_log():

    '''
    LOG.info("Giving handle (fileId:" + handle.getFileId()
        + ") to client for export " + path);        
    }
    '''
    #line = '(115: 5) LOG.info("Giving handle (fileId:" + handle.getFileId()'
    PATH='/home/mtoth/skola/dp/hadoop-common/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/org/apache/hadoop/hdfs/nfs/mount/RpcProgramMountd.java'
    POS=[115, 5]
    OLD='LOG.info("Giving handle (fileId:" + handle.getFileId()'
    NEW='LOG.LOG(path, e).tag("org.apache.hadoop.hdfs.nfs").error();' # it;s previous log!!

    f = open(PATH, "r+b")
    try:
        java_file = list(enumerate(f))        
        log = ""       

        for i, line in java_file[POS[0]-1:]:
            line = line.strip()
            log = log + line
            if ";" in line:
                # if line is not COMMENT - break! else ignore line
                if "//" not in line:
                    break

        print "log = ", log
    except:
        print 'Error while reading file!', PATH
        raise
    finally:
        f.close()
        return log



fetch_full_log()