import { z } from "zod";

/** ------------------------------------------------ */
/** ----------------- Preprocessers ----------------- */
/** ------------------------------------------------ */

/**
 * Query params are always strings
 * These helpers will preprocess query params into their proper types for validation
 */

export const zodPreprocessBoolean = () =>
  z.preprocess((value) => {
    const processed = z
      .string()
      .transform((val) => val === "true")
      .safeParse(value);
    return processed.success ? processed.data : value;
  }, z.boolean());

export const zodPreprocessNumber = (min: number = 0, max: number = Number.MAX_SAFE_INTEGER) =>
  z.preprocess((value) => {
    const processed = z.string().transform(Number).safeParse(value) || 0;
    return processed.success ? processed.data : value;
  }, z.number().min(min).max(max));

export const zodLowercaseString = () =>
  z.preprocess((value) => {
    const processed = z
      .string()
      .transform((s) => s.toLowerCase())
      .safeParse(value);
    return processed.success ? processed.data : value;
  }, z.string());

export const zodPreprocessDate = () =>
  z.preprocess((value) => {
    const processed = z
      .string()
      .transform((val) => new Date(val))
      .safeParse(value);
    return processed.success ? processed.data : value;
  }, z.date());

export const zodPreprocessAuthToken = () =>
  z.preprocess(
    (value) => {
      const processed = z
        .string()
        .transform((val) => {
          const [username, password] = Buffer.from(val.split(" ")[1], "base64").toString().split(":");

          return { username, password };
        })
        .safeParse(value);

      return processed.success ? processed.data : value;
    },
    z.object({ username: z.string(), password: z.string() }),
  );
