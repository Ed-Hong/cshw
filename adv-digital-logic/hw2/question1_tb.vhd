library ieee ;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.ALL;
use ieee.std_logic_unsigned.all;

entity question1_tb is
end question1_tb;

architecture tb of question1_tb is
    -- dummy component
    component test
        port(   clk: in     std_logic;  )
    end test;
    
    signal      clk:        std_logic := '0';
    constant    period:     time := 10 ns;
    constant    n:          integer :=4;                         -- number of pins
    signal      s:          std_logic_vector (n-1 downto 0);     -- vector representing input state
begin
    uut: test PORT MAP (clk => clk);       

    -- Initial input state
    s <= '0';
    wait for 10 ns;


    clk_gen: 
    process begin
        clk <= not clk;
        wait for period;
    end process;

    tb: 
    process (clk) begin
        if (rising_edge(clk)) then
            s <= s + '1';                       -- "+" operator overloaded from std_logic_arith library
            wait for period;
        end if;
    end process;
end tb;