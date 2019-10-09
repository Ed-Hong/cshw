module question3_dff (
    input clk,
    input rst,
    input d,
    output reg q
    );
 
    always @(negedge clk or posedge rst) begin
        if (rst) 
            q <= 0;
        else
            q <= d;
    end
 
endmodule