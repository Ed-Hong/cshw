module question2_counter8b (
    input clk,
    input rst,
    output reg [7:0] count
    );

    // init counter to 0
    initial
    count = 0;

    // Increment counter on each rising clock edge
    // OR reset counter to 0 when rst is high
    always @(posedge clk or posedge rst) begin
        if (rst)
            count = 0;
        else
            count = count + 1;
    end

endmodule