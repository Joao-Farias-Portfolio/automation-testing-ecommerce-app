import type { CartProtocol } from "./cartProtocol";
import type { SavedProtocol } from "./savedProtocol";

export interface MyEcommerceProtocol extends CartProtocol, SavedProtocol {}
