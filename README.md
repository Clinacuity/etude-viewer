# ETUDE Viewer
ETUDE Viewer -- a visual representation of some reference document vs. a target document.

#### Key ETUDE Characteristics:
- has command-line and GUI flavors
- converts multiple disparate input formats (e.g., line delimited sentences, inline and offset XML annotations) to a single unified back-end
- supports configurable extraction options to accommodate new formats
- ships with configurations for scoring several i2b2 NLP challenges
- has template configurations for common XML system outputs (e.g., UIMA CAS XMI)
- offers drag-and-drop creation of evaluation configuration files (i.e., specification of annotation categories and attributes to match and compare)
- allows file and annotation filtering fast prototyping
- automagically matches file names between folders
- supports three grades of overlap matching: exact, partial, and fully-contained
- generates type frequency counts, a confusion matrix, or evaluation metrics (e.g., true positives, false positives, recall)
- prints results to screen, file, or both
- shows side-by-side reference and system annotations in the original document context with easy exploration of matches and errors

#### Download & Run
Click [this link](https://s3.amazonaws.com/clinacuity/public-downloads/EtudeViewer.zip "Download ETUDE") to download a zip file containing the executable.

To run the application, unzip the file and double-click the file named _EtudeViewer.jar_.
