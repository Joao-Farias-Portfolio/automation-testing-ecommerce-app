/** @type {import('@cucumber/cucumber').IConfiguration} */
const config = {
  paths: ["features/**/*.feature"],
  require: ["src/**/*.ts"],
  requireModule: ["ts-node/register"],
  tags: process.env.TAGS ?? "not @wip",
  worldParameters: { channel: process.env.CHANNEL ?? "Web" },
  format: ["progress"],
};

module.exports = { default: config };
