/*
 * Author: Edward Hong
 * CE 6302.001 
 * Assignment 2: Questions 1 - 3
 * 1) Reading file and counting the number of lines
 * 2) Finding average of numbers in the file
 * 3) Finding min, max, variance, and std deviation
 * 
 * NOTE that this program assumes an input file of the same structure as the one provided.
 * ie - input files should contain two integer numbers separated by a space, for each line in the file.
 */
#include <stdlib.h>
#include <math.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

int lines = 0;                      // count of number of lines
float sum = 0.0;                    // running sum of all the numbers
int n = 0;                          // count of numbers
int min = INT_MAX;                  // running minimum
int max = INT_MIN;                  // running maximum
std::vector<int> nums;

void updateMin(int num) {
    if(num < min) {
        min = num;
    }
}

void updateMax(int num) {
    if(num > max) {
        max = num;
    }
}

int main(int argc, char** argv) {
    std::cout << "--------- Assignment 2 : Questions 1 - 3 ---------" << std::endl;
    std::cout << "--------- Author: Edward Hong --------------------" << std::endl;
    std::cout << std::endl;     // line break

    if(argc == 1) {
        std::cout << "Please pass the name of the file as a command line argument." << std::endl;
        return 0;
    }

    // Read the numbers from the file and count the number of lines.
    std::string fileName = argv[1];
    std::cout << "Reading " << fileName << "..."<< std::endl;
    std::ifstream infile(fileName);
    std::string line;
    while (std::getline(infile, line)) {

        // Grab the pair of numbers from the line
        std::istringstream iss(line);
        int a, b;
        if (!(iss >> a >> b)) { 
            std::cout << "Error occured reading file. Please make sure the format of the file is correct." << std::endl;
            return 0; 
        }

        lines++;                        // increment line counter
        sum += a + b;                   // update sum
        nums.push_back(a);              // add a to the nums vector
        nums.push_back(b);              // add b to the nums vector
    }

    n = 2 * lines;                      // count of numbers is equal to twice the number of lines
    float avg = (float) (sum / n);      // compute avg

    // Computing variance and std deviation, as well as finding min and max
    float var_sum = 0.0;
    for (int i = 0; i < nums.size(); i++) {
        float num = (float) nums[i];

        // If nums[i] is the new minimum or the new maximum, update it.
        updateMin(num);
        updateMax(num);
 
        var_sum += pow(num - avg, 2);
    }
    float variance = var_sum / (float)n - 1.0;
    float std_dev = sqrt(variance);

    // Printing statistics
    std::cout << fileName << " contains " << lines << " lines." << std::endl;
    std::cout << "Average: " << avg << std::endl;
    std::cout << "Min: " << min << std::endl;
    std::cout << "Max: " << max << std::endl;
    std::cout << "Variance: " << variance << std::endl;
    std::cout << "Standard Deviation: " << std_dev << std::endl;
    std::cout << std::endl;

}