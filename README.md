The OCL-SwingjS project is a fork of the [OpenChemLib Java project](https://github.com/Actelion/openchemlib) that optimizes that code for the [java2script Eclipse plug-in transpiler and the SwingJS JavaScript runtime library](https://github.com/BobHanson/java2script). The transpiler allows for a common codebase for Java and JavaScript a bit like GWT but far more standard and capable, particularly in that it allows for Swing, has better handling of long number types, includes more extensions to and beyond Java 8, and allows for fully Java-compliant dynamic class loading and packaging. Simultaneous transpiling to JavaScript using an Eclipse Java compiler "[CompilationParticipant](https://github.com/BobHanson/java2script/blob/master/sources/net.sf.j2s.core/src/j2s/core/Java2ScriptCompilationParticipant.java)" means active-page comparisons in Java and JavaScript functionality and performance. No functionality of the Java is lost (although the JFX GUI Java option is not included as an option in JavaScript; Swing is sufficient). For more examples of the success of java2script/SwingJS, see the [Working Examples](https://github.com/BobHanson/java2script/tree/master?tab=readme-ov-file#working-examples) links on that project site.

In addition, this project is a testbed for adding new features to the OpenChemLib project proper. Recent additions here that might show up in the future at the OpenChemLib project include:

- fixes for the SMILES Parser to handle allene stereochemistry properly (now part of OpenChemLib)
- adds [CDX and CDXML](https://iupac.github.io/IUPAC-FAIRSpec/cdx_sdk/) file parsers (now part of OpenChemLib)
- adds StereoMolecule-to-InChI borrowing and adapting from [JNI-InChI](https://github.com/SureChEMBL/jni-inchi) (Java) and [InChI-WEB](https://github.com/IUPAC-InChI) (JavaScript WASM)
- adds InChI-to-StereoMolecule (full stereochemistry for tetrahedral, double bond, and allene; uses just the standard InChI string, no auxInfo)
- adds InChI-to-InChI (allowing, for example for tautomers, the determination of the preferred internal InChI tautomer model)
- removal of principal long integer dependence (just for a bit faster performance; SwingJS already handles Long as a simple Int32Array[3] value)


Provided below is the original REDAME.md file for the OpenChemLib project. 

## OpenChemLib
*OpenChemLib* is Java based framework providing cheminformatics core functionality and user interface components. Its main focus is on organics chemistry and small molecules. It is built around a StereoMolecule class, which represents a molecule using atom and bond tables, provides atom neighbours, ring and aromaticity information, and supports MDL's concept of enhanced stereo representation. Additional classes provide, 2D-depiction, descriptor calculation, molecular similarity and substructure search, reaction search, property prediction, conformer generation, support for molfile and SMILES formats, energy minimization, ligand-protein interactions, and more. *OpenChemLib's idcode* represents molecules, fragments or reactions as canonical, very compact string that includes stereo and query features.
Different to other cheminformatics frameworks, *OpenChemLib* also provides user interface components that allow to easily embed chemical functionality into Java applications, e.g. to display or edit chemical structures or reactions.

### Dependencies
*OpenChemLib* requires JRE 8 or newer including JavaFX. Otherwise, there are no dependencies.

### How to download the project
```bash
git clone https://github.com/Actelion/openchemlib.git
```

### Build the project
To build the project with maven run the following from within the project directory:
```bash
./mvnw package
```
To build the project using the JDK only (Mac, Linux) run this from within the project directory:
```
./buildOpenChemLib
```

### Folder 'examples'
Contains examples for working with the *OpenChemLib* library.

### Logo
![logo](logo.png)
