where-am-i = $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
THIS_MAKEFILE := $(call where-am-i)
#$(info $(THIS_MAKEFILE))
ROOT := $(dir $(THIS_MAKEFILE))
$(info "I am in " $(ROOT))

#ROOT:=$(shell echo $(ROOT) | sed -e 's/\/\//\//g' -e 's/\/$$//g' )
JAVAC?=javac

SRC_JAVA:=$(ROOT)/src/main/java
SRC_ROOTS:= $(SRC_JAVA)
JVM_VERSION:=11

JAVA_BUILD:=$(ROOT)/WebContent/WEB-INF/classes

all: deploy

.PHONY: prep-java-list java-compile clean war copy-resources

deploy: war
	cp $(ROOT)/target/pill.war /opt/tomcat/webapps/ROOT.war
local: war
	cp $(ROOT)/target/pill.war /opt/tomcat/webapps/ROOT.war
	$(ROOT)/bounce.sh
pi: war
	cat $(ROOT)/target/pill.war |ssh pi@192.168.25.1 'cat > /opt/tomcat/webapps/ROOT.war ; rm -rf /opt/tomcat/webapps/ROOT ; sudo systemctl restart tomcat ; sudo systemctl restart pill-x' 

local: war
	cat $(ROOT)/target/pill.war |sudo su pi -c 'cat > /opt/tomcat/webapps/ROOT.war ; rm -rf /opt/tomcat/webapps/ROOT ; sudo systemctl restart tomcat ; sudo systemctl restart pill-x'


LIB_LIST:=$(shell find $(ROOT)/WebContent/WEB-INF/lib -name '*.jar' -printf '%p:')
$(info "My Lib List " $(LIB_LIST))
LIB_LIST:=$(LIB_LIST):$(ROOT)/lib/servlet-api.jar
$(info "New Lib List " $(LIB_LIST))

java-compile : 
	@echo Compile java
	@rm -rf $(JAVA_BUILD)
	@mkdir -p $(JAVA_BUILD)
	rm -f $(JAVA_BUILD)/java_list
	@echo JAVAC VERSION
	$(JAVAC) -version
	find $(SRC_JAVA) -name '*.java' > $(JAVA_BUILD)/java_list
	$(JAVAC) -encoding utf8 -source $(JVM_VERSION) -target $(JVM_VERSION) \
	-cp \
	$(LIB_LIST) \
	-d $(JAVA_BUILD) @$(JAVA_BUILD)/java_list
	@rm -f $(JAVA_BUILD)/java_list

war:  java-compile
	rm -rf $(ROOT)/target
	mkdir -p $(ROOT)/target
	sh -c "cd $(ROOT)/WebContent && zip -qr $(ROOT)/target/pill.war . "

clean:
	rm -rf $(ROOT)/javabuild $(ROOT)/java_list $(ROOT)/target
	