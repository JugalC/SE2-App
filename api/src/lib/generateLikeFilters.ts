import { SQL, and, ilike } from "drizzle-orm";
import { SQLiteColumn } from "drizzle-orm/sqlite-core";

export const generateLikeFilters = (filters: { col: SQLiteColumn; val?: string | null }[]) => {
  const where: SQL[] = [];

  for (const { col, val } of filters) {
    if (val) {
      where.push(ilike(col, val));
    }
  }

  return and(...where);
};
