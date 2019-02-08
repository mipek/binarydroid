# BinaryDroid

## Description

This is a simple disassembler application (currently ARM only) for android. It provides basic functionality out of the box but is build with extensibility in mind.
The application was created as part of the "Android Programming" module at TH-Wildau, Germany.

It currently uses Capstone for disassembling under the hood.

## Features
- Disassemble a file from the file system or a binary from an installed application (SU required).
 - A hexview that displays both the address and ASCII characters (in landscape mode) at a given address
 - Of course, a disassembler that shows mnemonics instead of bytes
 - Symbol list
- Search every installed application for a specified symbol (SU required).

## Dependencies
This application utilizes the following open-source libraries:
- Capstone
- jelf
- RootManager

## Todo
I have a couple of things in mind that would be cool to add in future:
- Add support for different "Container types" (ie. not just ELF files but PE files too)
- Improve architecture support (add x86, mips, ....)
- Implement mode switching (ARM and Thumb) - should be easy enough given that we use Capstone
- Graph-View disassembler (as seen in IDA or x64dbg)