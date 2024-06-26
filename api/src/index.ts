import fastify from "fastify";
import fastifyCron from "fastify-cron";
import { serializerCompiler, validatorCompiler, ZodTypeProvider } from "fastify-type-provider-zod";
import { users } from "./routes/users";
import { spotify } from "./routes/spotify";
import { friendships } from "./routes/friendships";
import { likes } from "./routes/likes";
import { comments } from "./routes/comments";
import { posts } from "./routes/posts";
import { notificationJob } from "./lib/sendNotification";

const server = fastify().withTypeProvider<ZodTypeProvider>();

server.setValidatorCompiler(validatorCompiler);
server.setSerializerCompiler(serializerCompiler);

server.register(users);
server.register(spotify);
server.register(friendships);
server.register(likes);
server.register(comments);
server.register(posts);

server.register(fastifyCron, {
  jobs: [notificationJob],
});

server.get("/ping", async () => {
  return "pong\n";
});

server.listen({ port: 8080 }, (err, address) => {
  if (err) {
    console.error(err);
    process.exit(1);
  }
  server.cron.startAllJobs();
  console.log(`Server listening at ${address}`);
});
