module question3_divby2 (
    input clk,
    input rst,
    output reg f
    );

    reg qnot; 
    wire _q;

    question3_dff dff(
        .clk(clk),
        .rst(rst),
        .d(qnot),
        .q(_q)
    );

    always @(negedge clk or posedge rst) begin
        if (rst) 
            qnot <= 0;
        else
            qnot <= !_q;
        f <= _q;
    end
 
endmodule