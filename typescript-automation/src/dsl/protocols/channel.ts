export type Channel = "Web" | "API";

let resolved: Channel | undefined;

export function currentChannel(): Channel {
  if (resolved !== undefined) return resolved;
  const raw = process.env["CHANNEL"];
  if (!raw || raw.trim() === "") {
    throw new Error("Environment variable CHANNEL is required. Valid values: Web, API");
  }
  const trimmed = raw.trim();
  if (trimmed !== "Web" && trimmed !== "API") {
    throw new Error(`Unknown channel '${trimmed}'. Valid values: Web, API`);
  }
  resolved = trimmed;
  return resolved;
}
