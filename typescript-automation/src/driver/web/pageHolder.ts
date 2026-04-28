import type { Page } from "playwright";

let currentPage: Page | undefined;

export function setPageHolder(page: Page): void {
  currentPage = page;
}

export function getPageHolder(): Page {
  if (currentPage === undefined) {
    throw new Error("Playwright page has not been initialised. Is the Web channel hook running?");
  }
  return currentPage;
}

export function clearPageHolder(): void {
  currentPage = undefined;
}
