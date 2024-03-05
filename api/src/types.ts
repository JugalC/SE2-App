import { FastifyPluginCallback, FastifyPluginOptions, RawServerDefault, FastifyBaseLogger } from "fastify";
import { ZodTypeProvider } from "fastify-type-provider-zod";
import { z } from "zod";
import { zodPreprocessNumber } from "./lib/zodHelpers";

export type Plugin = FastifyPluginCallback<FastifyPluginOptions, RawServerDefault, ZodTypeProvider, FastifyBaseLogger>;

export const paginationSchema = z.object({
  page: zodPreprocessNumber().optional().default(0),
  limit: zodPreprocessNumber().optional().default(100),
});

export const searchSchema = z.object({
  search: z.string().optional().default(""),
});