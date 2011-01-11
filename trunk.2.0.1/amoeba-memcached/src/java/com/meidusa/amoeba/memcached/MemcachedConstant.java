package com.meidusa.amoeba.memcached;

public interface MemcachedConstant {
	static final int HEAD_SIZE = 0;

	byte COMMAND_GET = 0x00;
	byte COMMAND_SET = 0x01;
	byte COMMAND_Add = 0x02;// Add
	byte COMMAND_Replace = 0x03;// Replace
	byte COMMAND_Delete = 0x04;// Delete
	byte COMMAND_Increment = 0x05;// Increment
	byte COMMAND_Decrement = 0x06;// Decrement
	byte COMMAND_Quit = 0x07;// Quit
	byte COMMAND_Flush = 0x08;// Flush
	byte COMMAND_GetQ = 0x09;// GetQ
	byte COMMAND_Noop = 0x0A;// No-op
	byte COMMAND_Version = 0x0B;// Version
	byte COMMAND_GetK = 0x0C;// GetK
	byte COMMAND_GetKQ = 0x0D;// GetKQ
	byte COMMAND_Append = 0x0E;// Append
	byte COMMAND_Prepend = 0x0F;// Prepend
	byte COMMAND_Stat = 0x10;// Stat
	byte COMMAND_SetQ = 0x11;// SetQ
	byte COMMAND_AddQ = 0x12;// AddQ
	byte COMMAND_ReplaceQ = 0x13;// ReplaceQ
	byte COMMAND_DeleteQ = 0x14;// DeleteQ
	byte COMMAND_IncrementQ = 0x15;// IncrementQ
	byte COMMAND_DecrementQ = 0x16;// DecrementQ
	byte COMMAND_QuitQ = 0x17;// QuitQ
	byte COMMAND_FlushQ = 0x18;// FlushQ
	byte COMMAND_AppendQ = 0x19;// AppendQ
	byte COMMAND_PrependQ = 0x1A;// PrependQ
}