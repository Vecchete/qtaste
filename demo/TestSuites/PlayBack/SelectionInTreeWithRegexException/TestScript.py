# encoding= utf-8

##
# Playback/SelectionInTreeWithRegexException test
# <p>
# Test error cases for the node selection in a tree using regex.
#
##

from qtaste import *

import time

# update in order to cope with the javaGUI extension declared in your testbed configuration.
javaguiMI = testAPI.getJavaGUI(INSTANCE_ID=testData.getValue("JAVAGUI_INSTANCE_NAME"))

subtitler = testAPI.getSubtitler()
subtitler.setSubtitle(testData.getValue("COMMENT"))

# select the tab with tree components
javaguiMI.selectTabId("TABBED_PANE", "TREE_LIST_PANEL")

# get test data
component   = testData.getValue("COMPONENT_NAME")
value       = testData.getValue("VALUE")
separator   = testData.getValue("SEPARATOR")
expectedMsg = testData.getValue("EXPECTED_MESSAGE")

def reset():
    """
    @step      clear the node selection
    @expected no node should be selected
    """
    javaguiMI.clearNodeSelection(component)

def step1():
    """
    @step      select a node according to test data
    @expected  a QTasteTestFailException with a message
    """
    try:
        javaguiMI.selectNodeRe(component, value, separator)
    except QTasteTestFailException, e:
        if e.message != expectedMsg:
            testAPI.stopTest(Status.FAIL, "Expected message : '" + expectedMsg + "' but got : '" + e.message + "'")
    except e:
        testAPI.stopTest(Status.FAIL, "Unexpected exception : " + repr(e))
    
    time.sleep(1)

doStep(reset)
doStep(step1)
