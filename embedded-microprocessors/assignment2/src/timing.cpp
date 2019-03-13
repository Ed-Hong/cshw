/*
 * Author: Edward Hong
 * CE 6302.001 
 * Assignment 2: Question 5
 * 
 * Timing the execution of the following operations:
 * Integer: +, -, *, /, %
 * Float: +, - , *, /
 */

#include <algorithm> 
#include <chrono> 
#include <iostream> 

using namespace std::chrono; 

int main() { 
    std::cout << "--------- Assignment 2 : Question 5 --------------" << std::endl;
    std::cout << "--------- Author: Edward Hong --------------------" << std::endl;
    std::cout << std::endl;     // line break

    // Random integers
    int int_a = rand();
    int int_b = rand();

    // Random floating point numbers between 0.0 and 10.0
    float fl_a = (float) (rand()) / (static_cast <float> (RAND_MAX/10.0));
    float fl_b = (float) (rand()) / (static_cast <float> (RAND_MAX/10.0));

    // -------------------- Integer + --------------------
    // START TIME
    auto start = high_resolution_clock::now(); 
  
    int ans = int_a + int_b;

    // STOP TIME
    auto stop = high_resolution_clock::now(); 
    
    auto duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Integer add took " << duration.count() << " nanoseconds" << std::endl; 

    // -------------------- Integer - --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    ans = int_a - int_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Integer subtract took " << duration.count() << " nanoseconds" << std::endl; 

    // -------------------- Integer * --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    ans = int_a * int_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Integer multiply took " << duration.count() << " nanoseconds" << std::endl;

    // -------------------- Integer / --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    ans = int_a / int_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Integer divide took " << duration.count() << " nanoseconds" << std::endl;

    // -------------------- Integer % --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    ans = int_a % int_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Integer modulus took " << duration.count() << " nanoseconds" << std::endl;

    std::cout << std::endl;     // line break

    // -------------------- Float + --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    float result = fl_a + fl_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Float add took " << duration.count() << " nanoseconds" << std::endl;
    
    // -------------------- Float - --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    result = fl_a - fl_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Float subtract took " << duration.count() << " nanoseconds" << std::endl;

    // -------------------- Float * --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    result = fl_a * fl_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Float multiply took " << duration.count() << " nanoseconds" << std::endl;

    // -------------------- Float / --------------------
    // START TIME
    start = high_resolution_clock::now(); 
  
    result = fl_a / fl_b;

    // STOP TIME
    stop = high_resolution_clock::now(); 
    
    duration = duration_cast<nanoseconds>(stop - start); 
    std::cout << "Float division took " << duration.count() << " nanoseconds" << std::endl;

    std::cout << std::endl;     // line break

    return 0; 
} 