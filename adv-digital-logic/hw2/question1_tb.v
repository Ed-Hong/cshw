module question1_tb;

    // Inputs
    reg clk;

    // Outputs

    // dummy test module
    test uut (
      .clk(clk)
    );


    // --- Begin Test Function ------------

    // number of input pins
    parameter n = 4;

    // n-bit register representing current input state
    reg [n:0] state;
    
    initial begin
        // init input state
        state = 0;
        clk = 0;
    end

    // clock generation
    always begin
        #10 clk = !clk;
    end

    // incrementally check all states
    always (@posedge clk) begin
        state = state + 1;
        #10;
    end 

endmodule