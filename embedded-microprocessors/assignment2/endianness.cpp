/*
 * Author: Edward Hong
 * CE 6302.001 
 * Assignment 2: Question 4
 * 
 * The following program will print the endianness of the machine on which the program is run.
 * It does this by assigning a char pointer (1 byte) a value of integer 1 and checking the value 
 * of the byte which was assigned.
 * 
 * When de-referencing the char pointer, if the stored byte is 1, then we know that the machine is
 * little endian, since the least significant byte of the int was stored into the char pointer. 
 * However,if the stored byte is 0, then we know that the machine is big endian, as then the most 
 * significant byte was stored into the char pointer.
 */

#include <stdio.h>
#include <iostream>

int main() {
    std::cout << "--------- Assignment 2 : Question 4 --------------" << std::endl;
    std::cout << "--------- Author: Edward Hong --------------------" << std::endl;
    std::cout << std::endl;     // line break

    unsigned int i = 1;                  // i = 0x0001
    char *c = (char*) &i;                // assign char pointer (1 byte) value of 0x0001 
    if (*c) {

        // if char pointer is 0x1 then machine is little endian
        std::cout << "This machine is LITTLE ENDIAN" << std::endl;

    } else {

        // else char pointer is 0x0 so machine is big endian
        std::cout << "This machine is BIG ENDIAN" << std::endl;

    }
    
   return 0; 
} 