# BinaryDroid

## Description

This is a simple disassembler application (currently ELF & ARM only) for android. It provides basic functionality out of the box but is build with extensibility in mind.
The application was created as part of the "Android Programming" module at TH-Wildau, Germany.

It currently uses Capstone for disassembling under the hood.

This application currently only runs on ARM devices.

## Features
- Disassemble a file from the file system or a binary from an installed application (SU required).
 - A hexview that displays both the address and ASCII characters (in landscape mode) at a given address
 - Of course, a disassembler that meaningfull stuff instead of bytes
 - Symbol list
- Search every installed application for a specified symbol (SU required).
- Patching files
- Saving the modified file

## Dependencies
This application utilizes the following open-source libraries:
- [Capstone](https://github.com/aquynh/capstone) (disassembly/disassembler framework)
- [jelf](https://github.com/fornwall/jelf) (ELF parsing library in java)
- [RootManager](https://github.com/Chrisplus/RootManager) (helper utility for SU functionality)

## Todo
I have a couple of things in mind that would be cool to add in future versions:
- Add support for different "container types" (ie. not just ELF files but PE files too)
- Improve architecture support (add x86, mips, ....)
- Implement mode switching (ARM and Thumb) - should be easy enough given that we use Capstone
- Graph-View disassembler (as seen in IDA or x64dbg)
- Editing bytes in the HexView (essentially turning it into a HexEditor)
- A more sophisticated assembler engine (currently we shoehorn the disassembler to do this for us)
- Maybe add option to sync scrolling in the disassembler with hexview and vice versa?
- More options for the disassembler (show opcode bytes, instruction indentation etc.)
- A interactive disassembler (follow jumps etc.)
- Syntax coloring

## Screenshot

![installed apps](https://abload.de/img/installedapps9fj9h.png)
![disassembler](https://abload.de/img/disassemblerh4jxp.png)