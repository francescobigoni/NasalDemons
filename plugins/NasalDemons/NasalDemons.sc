NasalDemons : MultiOutUGen {
	*ar {|numChannels, block, size=1, loop=1, rate=1, post=0|
		var addr = block.asArray.collect(_.addrLoForUGen).flop.collect(_.unbubble);
		var blockSize = (block.asArray.collect(_.bytes) * size).unbubble;
		^this.multiNew(*(['audio', numChannels] ++ addr ++ [ blockSize, loop, rate, post]));
	}

  init { arg argNumChannels ... theInputs;
		inputs = theInputs;
		^this.initOutputs(argNumChannels, rate);
	}
	argNamesInputsOffset { ^2 }

	// Get Memory Addresses

	*getMemoryBlocks{
		^switch(thisProcess.platform.name)
		{\linux}{this.prGetMemBlocksLinux}
		{\osx}{this.prGetMemBlocksOSX}
	}

	*getHeapBlocks{
		^switch(thisProcess.platform.name)
		{\linux}{this.prGetHeapBlocksLinux}
		{\osx}{this.prGetHeapBlocksOSX}
	}

	*getStackBlocks{
		^switch(thisProcess.platform.name)
		{\linux}{this.prGetStackBlocksLinux}
		{\osx}{this.prGetStackBlocksOSX}
	}

	*procMapsToBlocks{|lines|
		^lines.collect(_.split($-)).collect(NasalDemonsMemBlock(*_))
	}

	// Platform: LINUX

	*prGetMemBlocksLinux{
		^this procMapsToBlocks: "cat /proc/%/maps | grep '[^ ] r[w-][x-][p-] 00000000' | awk -F' ' '{print $1}'".format(Server.default.pid).unixCmdGetStdOutLines;
	}
	*prGetHeapBlocksLinux{
		^this procMapsToBlocks: "cat /proc/%/maps | grep 'heap' | awk -F' ' '{print $1}'".format(Server.default.pid).unixCmdGetStdOutLines
	}
	*prGetStackBlocksLinux{
		^this procMapsToBlocks: "cat /proc/%/maps | grep 'stack' | awk -F' ' '{print $1}'".format(Server.default.pid).unixCmdGetStdOutLines
	}

	// Platform: OSX

	*prGetMemBlocksOSX{
		^this procMapsToBlocks: "vmmap % | grep \"r[w-][x-]/r[w-][x-]\" | grep -o '[0-9a-fA-F]\\{16\\}-[0-9a-fA-F]\\{16\\}'".format(Server.default.pid).unixCmdGetStdOutLines;
	}
	*prGetHeapBlocksOSX{
		^this procMapsToBlocks: "vmmap % | grep \"^MALLOC\" | grep \"r[w-][x-]/r[w-][x-]\" | grep -o \"[0-9a-fA-F]\\{16\\}-[0-9a-fA-F]\\{16\\}\"".format(Server.default.pid).unixCmdGetStdOutLines
	}
	*prGetStackBlocksOSX{
		^this procMapsToBlocks: "vmmap % | grep \"^Stack\" | grep \"r[w-][x-]/r[w-][x-]\" | grep -o \"[0-9a-fA-F]\\{16\\}-[0-9a-fA-F]\\{16\\}\"".format(Server.default.pid).unixCmdGetStdOutLines
	}

	checkInputs {
		/* TODO */
		^this.checkValidInputs;
	}
}

NasalDemonsMemBlock {
	var <>addrLo, <>addrHi;

	*new {|addrLo, addrHi|
		^super.newCopyArgs(addrLo,addrHi)
	}

	*hexToInt {|hexString|
		^ hexString.inject(0){ |result, char|
  			if(char.digit.notNil){
        		(result << 4) | char.digit;
    		} {
        		"Invalid hex digit found at % in %".format(char, hexString).warn;
        		result << 4;
    		};
		};
	}

	addrLoForUGen{
		^ addrLo.padLeft(16,"0").clump(4).collect(this.class.hexToInt(_))
	}

	bytes{
		^ [addrLo,addrHi].collect(this.class.hexToInt(_)).differentiate[1]
	}

	asString {
		^ "a %( %, % )".format(this.class, addrLo, addrHi)
	}
	debug {
		"a %[%-%](% bytes)".format(this.class, addrLo, addrHi, this.bytes).postln
	}

}
