import { z } from 'zod';

/** ------------------------------------------------ */
/** ----------------- Preprocessers ----------------- */
/** ------------------------------------------------ */

/**
 * Query params are always strings
 * These helpers will preprocess query params into their proper types for validation
 */

export const zodPreprocessBoolean = () =>
  z.preprocess(value => {
    const processed = z
      .string()
      .transform(val => val === 'true')
      .safeParse(value);
    return processed.success ? processed.data : value;
  }, z.boolean());

export const zodPreprocessNumber = () =>
  z.preprocess(value => {
    const processed = z.string().transform(Number).safeParse(value) || 0;
    return processed.success ? processed.data : value;
  }, z.number());

export const zodLowercaseString = () =>
  z.preprocess(value => {
    const processed = z
      .string()
      .transform(s => s.toLowerCase())
      .safeParse(value);
    return processed.success ? processed.data : value;
  }, z.string());

export const zodPreprocessDate = () =>
  z.preprocess(value => {
    const processed = z
      .string()
      .transform(val => new Date(val))
      .safeParse(value);
    return processed.success ? processed.data : value;
  }, z.date());
