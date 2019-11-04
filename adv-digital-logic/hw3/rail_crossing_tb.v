`timescale 1ns / 1ps

module rail_crossing_tb;

	// Inputs
	reg x;
	reg y;
	
	// Outputs
	wire c;

	// Instantiate the Unit Under Test (UUT)
	rail_crossing uut (
		.x(x), 
		.y(y), 
		.c(c)
	);
	
	parameter TEST_LEN = 5;
	reg [0:TEST_LEN-1] x_seq = 5'b01110;
	reg [0:TEST_LEN-1] y_seq = 5'b00100;
	integer i = 0 ;

	initial begin
		// Initialize Inputs
		x = 0;
		y = 0;

		// Wait 100 ns for global reset to finish
		#100;
        
		// Add stimulus here
		for ( i = 0 ; i < TEST_LEN ; i = i + 1 )
		   begin
			   x <= x_seq[i] ;
				y <= y_seq[i] ;
				#100 ;
			end

	end
      
endmodule

