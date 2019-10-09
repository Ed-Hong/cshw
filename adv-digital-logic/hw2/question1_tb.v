////////////////////////////////////////////////
// Note that this generic testbench function 
// omits the code segments which would 
// instantiate the Unit Under Test (UUT)
////////////////////////////////////////////////

module question1_tb;

    // --- This section omitted -----------

    // Inputs

    // Outputs

    // Instantiate Unit Under Test (UUT)
    
    // --- End section -------------------


    // --- Begin Test Function ------------

    // number of input pins
    parameter n = 4;

    // n-bit register representing current input state
    reg [n:0] state;

    initial begin
        // init input state
        state = 0;
    end

   // incrementally check all states
   always
      #1 state = state + 1;

endmodule