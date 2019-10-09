------------------------------------------------
-- Note that this generic testbench function 
-- omits the code segments which would 
-- instantiate the Unit Under Test (UUT)
------------------------------------------------

library ieee ;
use ieee.std_logic_1164.all;
use IEEE.std_logic_arith.ALL;
use ieee.std_logic_unsigned.all;

entity question1_tb is
end question1_tb;

architecture tb of question1_tb is
    constant n: integer :=4;                    -- number of pins
    signal s: std_logic_vector(n-1 downto 0);   -- vector representing current input state
begin
    -- Instantiate UUT (omitted)
    -- uut: myuut port map (a => a, b => b, ... );

    -- Initial input state
    s <= '0';
    wait for 10 ns;

    tb: process
        constant period: time := 10 ns;
        begin
            s <= s + '1';                       -- "+" operator overloaded from std_logic_arith library
            wait for period;
        end process;
end tb;