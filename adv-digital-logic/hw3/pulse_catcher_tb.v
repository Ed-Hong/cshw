`timescale 1ns / 1ps

module pulse_catcher_tb;

	// Inputs
	reg p;
	reg r;

	// Outputs
	wire z;

	// Instantiate the Unit Under Test (UUT)
	pulse_catcher uut (
		.P(p),
		.R(r), 
		.Z(z)
	);
	
	parameter TEST_LEN = 10 ;
	reg [0:TEST_LEN-1] p_seq = 10'b0010011011;
	reg [0:TEST_LEN-1] r_seq = 10'b1000110001;
	integer i = 0 ;

	initial begin
		// Initialize Inputs
		p = 1;
		r = 1;

		// Wait 100 ns for global reset to finish
		#100;
        
		// Add stimulus here
		for ( i = 0 ; i < TEST_LEN ; i = i + 1 )
		   begin
			   p <= p_seq[i] ;
				r <= r_seq[i] ;
				#100 ;
			end

	end
      
endmodule

