const fs = require("fs");

const source = "docs/assets/streamguard-payment-flow.gif";
const buf = fs.readFileSync(source);

function splitHeader(input) {
  let i = 6;
  const packed = input[i + 4];
  i += 7;
  if (packed & 0x80) {
    i += 3 * (1 << ((packed & 7) + 1));
  }
  return i;
}

function inspect(input) {
  let i = splitHeader(input);
  let frames = 0;
  let loop = false;
  while (i < input.length) {
    if (input[i] === 0x3b) break;
    if (input[i] === 0x21) {
      const label = input[i + 1];
      i += 2;
      if (label === 0xff) {
        const size = input[i];
        const name = input.subarray(i + 1, i + 1 + size).toString("ascii");
        if (name.startsWith("NETSCAPE")) loop = true;
      }
      while (input[i] !== 0) i += input[i] + 1;
      i += 1;
      continue;
    }
    if (input[i] === 0x2c) {
      frames += 1;
      const packed = input[i + 9];
      i += 10;
      if (packed & 0x80) i += 3 * (1 << ((packed & 7) + 1));
      i += 1;
      while (input[i] !== 0) i += input[i] + 1;
      i += 1;
      continue;
    }
    throw new Error(`Unexpected GIF block ${input[i]} at ${i}`);
  }
  return { frames, loop };
}

let output = buf;
let info = inspect(output);
if (!info.loop) {
  const at = splitHeader(output);
  const ext = Buffer.concat([
    Buffer.from([0x21, 0xff, 0x0b]),
    Buffer.from("NETSCAPE2.0"),
    Buffer.from([0x03, 0x01, 0x00, 0x00, 0x00]),
  ]);
  output = Buffer.concat([output.subarray(0, at), ext, output.subarray(at)]);
  fs.writeFileSync(source, output);
  info = inspect(output);
}

const parentDir = "C:/Users/salma/OneDrive/Desktop/stream-/docs/assets";
fs.mkdirSync(parentDir, { recursive: true });
fs.copyFileSync(source, `${parentDir}/streamguard-payment-flow.gif`);
console.log(JSON.stringify({ bytes: output.length, ...info }));
