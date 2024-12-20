## OpenChemLib
*OpenChemLib* is Java based framework providing cheminformatics core functionality and user interface components. Its main focus is on organics chemistry and small molecules. It is built around a StereoMolecule class, which represents a molecule using atom and bond tables, provides atom neighbours, ring and aromaticity information, and supports MDL's concept of enhanced stereo representation. Additional classes provide, 2D-depiction, descriptor calculation, molecular similarity and substructure search, reaction search, property prediction, conformer generation, support for molfile and SMILES formats, energy minimization, ligand-protein interactions, and more. *OpenChemLib's idcode* represents molecules, fragments or reactions as canonical, very compact string that includes stereo and query features.
Different to other cheminformatics frameworks, *OpenChemLib* also provides user interface components that allow to easily embed chemical functionality into Java applications, e.g. to display or edit chemical structures or reactions.

### OCL-SwingJS

This OCL-SwingJS project was cloned from OCL in order to integrate OCL into the Jmol/JME SwingJS family. Minor adaptations have been made in order to enable the Java to be simultaneously transpiled into JavaScript, allowing a single 
code source for both Java .class files and JavaScript .js files.

This [SwingJS](https://github.com/BobHanson/java2script) fork of the project adds bits of Java 8+ that allow for working asynchronously with modal dialogs and associated file issues. It is still completely compatible with the original OCL code, just augmented a bit here and there. It runs smoothly in JavaScript and allows simultaneous testing and performance checks in both Java and JavaScript using the jav2script transpiler and SwingJS JavaScript "Java" runtime environment extensively developed at St. Olaf College.

The port has allowed all of the functionality and all of the Swing-based GUI capabilities of
OCL to be part of Jmol/Java and JSmol/JavaScript. 

This is different from *OpenChemLib JS* in that that system require two independent code bases with separate projects and compilations. Here we just have the one code base, and when Java files are compiled automatically when a Java source file is saved in Eclipse, matching JavaScript js files are immediately created. Thus, debugging in real time in Java and in JavaScript (in a browser) is possible. 

In addition, this port allowed additional features to OCL to be added from the Jmol project, specifically InChI-to-structure (Java only), and CDX/CDXML file reading. 

All SwingJS capability is supported; FX classes are not supported. Classes include Java 8+; new classes can be added at will.


### Dependencies
*OpenChemLib* requires JRE 8 or newer including JavaFX. Otherwise, there are no dependencies.

OCL-SwingJS adds jni-inchi.jar for InChI-to-structure. 


### How to download the project

In a browser, navigate to https://github.com/BobHanson/OCL-SwingJS and copy the url to the clipboard using the green Code button or just use this one:

https://github.com/BobHanson/OCL-SwingJS.git

In Eclipse, use File...Import...Project from Git...Clone URI

This will create the project have some default button clicking.


### Build the project

Note that Eclipse is necessary in order to 
Using Eclipse, run the following xml files in the main project directory. 

build-site.xml    

(creates a local site directory and unzips swingjs/swingjs-site.zip into it. This adds
the JavaScript equivalent of the JRE along with
the Java 8 language JavaScript class file equivalents)

build-swingjs.xml

Creates OCL-SwingJS.jar and OCL-SwingJS.zip.

build-core_all.xml

### Setting up Eclipse with the J2S transpiler


If you are interested in the JavaScript verion, you will need
to place j2s.core.jar the J2S transpiler plug-in into the eclipse/dropins/ folder and then restart Eclipse. You will find j2s.core.jar in the project swingjs/ folder.

You will know the plug-in is running if you do a clean build and refresh the Navigator panel and see a site/ directory populate. Test HTML files will be there, and if you have used build-site.xml, they should run. 

Note that the JavaScript version of jni-inchi does not allow access to the core InChI structure model that is used in OCL-SwingJS to create an OCL molecule. 



