module question3_divby2 (
    input clk,
    input rst,
    output reg f
    );

    reg qnot; 

    question3_dff dff(
        .clk(clk),
        .rst(rst),
        .d(qnot),
        .q(f)
    );

    always @(negedge clk or posedge rst) begin
        if (rst) 
            qnot <= 0;
        else
            qnot <= !q;
    end
 
endmodule