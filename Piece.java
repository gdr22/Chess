public class Piece {
   int type;
   boolean color;
   int number;
   
   public Piece(int t, boolean c, int num) {
      type = t;
      color = c;
      number = num;
   }
   
   /** Clone constructor */
   public Piece(Piece p) {
      type = p.type;
      color = p.color;
      number = p.number;
   }
   
   /** Converts a piece to a byte*/
   public byte toByte() {
      byte out = 0;
      
      //Bit 7 - Color
      //Bits 3 - 6 Number (4 bit)
      //Bits 0 - 2 Type (3 bit)
      
      out += color ? 128 : 0;
      
      out += number << 3;
      
      out += type;
      
      return out;
   }
   
   /** Creates a piece from a byte */
   public Piece(byte b) {
      byte in = b;
      
      //Check bit 7
      color = (b & 0b10000000) != 0;
      
      //Mask and shift out the piece number
      number = (b & 0b01111000) >> 3;
      
      //Mask out the piece type
      type = (b & 0b00000111);
   }
}