export interface BrowserPort {
  // Navigation
  navigateTo(url: string): Promise<void>;
  navigateBack(): Promise<void>;
  currentUrl(): Promise<string>;

  // Element state
  isVisible(css: string): Promise<boolean>;
  isPresent(css: string): Promise<boolean>;
  isEnabled(css: string): Promise<boolean>;
  isSelected(css: string): Promise<boolean>;
  count(css: string): Promise<number>;

  // Element content
  text(css: string): Promise<string>;
  attribute(css: string, attr: string): Promise<string>;

  // Nth-element queries (flat list, zero-based index)
  isNthEnabled(css: string, index: number): Promise<boolean>;
  isNthSelected(css: string, index: number): Promise<boolean>;
  nthAttribute(css: string, index: number, attr: string): Promise<string>;
  nthText(css: string, index: number): Promise<string>;

  // Scoped queries — within the nth element matching parentCss
  isSelectedWithin(parentCss: string, parentIndex: number, childCss: string): Promise<boolean>;
  attributeWithin(parentCss: string, parentIndex: number, childCss: string, attr: string): Promise<string>;

  // Actions
  click(css: string): Promise<void>;
  clickNth(css: string, index: number): Promise<void>;
  clickXpath(xpath: string): Promise<void>;
  sendKeys(css: string, text: string, submitAfter: boolean): Promise<void>;
  setReactInputValue(css: string, value: string): Promise<void>;

  // JavaScript extraction
  extractAllViaScript(script: string): Promise<ReadonlyArray<Readonly<Record<string, string>>>>;
  executeScript(script: string, ...args: unknown[]): Promise<unknown>;

  // Waits
  waitUntilVisible(css: string): Promise<void>;
  waitUntilPresent(css: string): Promise<void>;
  waitUntilCountMoreThan(css: string, count: number): Promise<void>;
  waitUntilUrlContains(fragment: string): Promise<void>;
  waitUntilUrlMatches(regex: string): Promise<void>;
  waitUntilAttributeChanges(css: string, index: number, attr: string, previousValue: string): Promise<void>;
  waitUntilAnyPresent(...cssList: string[]): Promise<void>;
  waitUntilCondition(condition: () => Promise<boolean>, timeoutMs: number): Promise<void>;
  tryWaitUntilPresent(css: string, timeoutMs: number): Promise<boolean>;
}
