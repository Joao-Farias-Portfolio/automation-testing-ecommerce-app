from automation.dsl.protocols.cart_protocol import CartProtocol
from automation.dsl.protocols.saved_protocol import SavedProtocol


class MyEcommerceProtocol(CartProtocol, SavedProtocol):
    pass
