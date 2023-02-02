## OpenChemLib
*OpenChemLib* is Java based framework providing cheminformatics core functionality and user interface components. Its main focus is on organics chemistry and small molecules. It is built around a StereoMolecule class, which represents a molecule using atom and bond tables, provides atom neighbours, ring and aromaticity information, and supports MDL's concept of enhanced stereo representation. Additional classes provide, 2D-depiction, descriptor calculation, molecular similarity and substructure search, reaction search, property prediction, conformer generation, support for molfile and SMILES formats, energy minimization, ligand-protein interactions, and more. *OpenChemLib's idcode* represents molecules, fragments or reactions as canonical, very compact string that includes stereo and query features.
Different to other cheminformatics frameworks, *OpenChemLib* also provides user interface components that allow to easily embed chemical functionality into Java applications, e.g. to display or edit chemical structures or reactions.

This fork of the project adds bits of Java that allow for working asynchronously with modal dialogs and associated file issues. It is still completely compatible with the original OCL code, just augmented a bit here and there. It runs smoothly in JavaScript and allows simultaneous testing and performance checks in both Java and JavaScript using the jav2script transpiler and SwingJS JavaScript "Java" runtime environment, but extensively developed at St. Olaf College.

All SwingJS capability is supported; FX classes are not supported.


### Dependencies
*OpenChemLib* requires JRE 8 or newer including JavaFX. Otherwise, there are no dependencies.

### How to download the project
```bash
git clone https://github.com/BobHanson/OCL-SwingJS.git
```

### Logo
![logo](logo.png)
