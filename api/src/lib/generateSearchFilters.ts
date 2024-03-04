import { SQL, ilike, or } from "drizzle-orm";
import { SQLiteColumn } from "drizzle-orm/sqlite-core";

export const generateSearchFilters = (filters: { col: SQLiteColumn; val?: string | null }[]) => {
  const where: SQL[] = [];

  for (const { col, val } of filters) {
    if (val) {
      where.push(ilike(col, `${val}%`));
    }
  }

  return or(...where);
};