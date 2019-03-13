/*
 * Author: Edward Hong
 * CE 6302.001 
 * Assignment 2: Question 6
 * 
 * The following program counts the number of recursive calls made until a stack overflow occurs.
 */

#include <stdio.h>
#include <iostream>

int calls = 0;                  // number of recursive calls

void recurse() {
    calls++;
    std::cout << calls << " calls." << std::endl;
    recurse();
}

int main() {
    std::cout << "--------- Assignment 2 : Question 6 --------------" << std::endl;
    std::cout << "--------- Author: Edward Hong --------------------" << std::endl;
    std::cout << std::endl;     // line break

    recurse();
    
    return 0; 
} 