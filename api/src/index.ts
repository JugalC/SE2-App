import fastify from "fastify";
import { serializerCompiler, validatorCompiler, ZodTypeProvider } from "fastify-type-provider-zod";
import { users } from "./routes/users";
import { spotify } from "./routes/spotify";
import { friendships } from "./routes/friendships";
import { likes } from "./routes/likes";
import { comments } from "./routes/comments";

const server = fastify().withTypeProvider<ZodTypeProvider>();

server.setValidatorCompiler(validatorCompiler);
server.setSerializerCompiler(serializerCompiler);

server.register(users);
server.register(spotify);
server.register(friendships);
server.register(likes);
server.register(comments);

server.get("/ping", async () => {
  return "pong\n";
});

server.listen({ port: 8080 }, (err, address) => {
  if (err) {
    console.error(err);
    process.exit(1);
  }
  console.log(`Server listening at ${address}`);
});
