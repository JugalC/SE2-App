import { FastifyPluginCallback, FastifyPluginOptions, RawServerDefault, FastifyBaseLogger } from "fastify";
import { ZodTypeProvider } from "fastify-type-provider-zod";
import { z } from "zod";

export type Plugin = FastifyPluginCallback<FastifyPluginOptions, RawServerDefault, ZodTypeProvider, FastifyBaseLogger>;

export const paginationSchema = z.object({
  page: z.number().min(0).optional().default(0),
  limit: z.number().max(100).optional().default(100),
});
