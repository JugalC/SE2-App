import { scrypt, randomBytes } from "crypto";

export const generateSalt = () => {
  return new Promise<string>((resolve, reject) => {
    randomBytes(64, (err, buf) => {
      if (err) {
        return reject(String(err));
      }

      return resolve(buf.toString("hex"));
    });
  });
};

export const hash = (password: string, salt: string) => {
  return new Promise<Buffer>((resolve, reject) => {
    scrypt(password, salt, 64, (err, key) => {
      if (err) {
        return reject(err);
      }

      return resolve(key);
    });
  });
};
