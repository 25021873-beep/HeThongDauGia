
import json

graph_path = r"E:\HeThongDauGia\HeThongDauGia\.understand-anything\intermediate\assembled-graph.json"
with open(graph_path, "r", encoding="utf-8") as f:
    graph = json.load(f)

edges = graph.get("edges", [])

layers_path = r"E:\HeThongDauGia\HeThongDauGia\.understand-anything\intermediate\layers.json"
with open(layers_path, "r", encoding="utf-8") as f:
    layers_data = json.load(f)
    
if isinstance(layers_data, dict) and "layers" in layers_data:
    layers_data = layers_data["layers"]
    
layers_clean = []
for l in layers_data:
    layers_clean.append({
        "id": l.get("id", ""),
        "name": l.get("name", ""),
        "description": l.get("description", "")
    })

dispatch = f'''Create a guided learning tour for this codebase.
Project root: E:\HeThongDauGia\HeThongDauGia
Write output to: E:\HeThongDauGia\HeThongDauGia/.understand-anything/intermediate/tour.json
Project: HeThongDauGia - Không có mô tả nào được cung cấp cho dự án này.
Languages: css, fxml, java, json, mf, properties, txt, unknown, xml, yaml

Nodes (all file-level nodes):
```json
[
  {
    "id": "file:src/main/java/org/example/dao/AutoBidDAO.java",
    "name": "AutoBidDAO.java",
    "filePath": "src/main/java/org/example/dao/AutoBidDAO.java",
    "summary": "L\u1edbp DAO (Data Access Object) qu\u1ea3n l\u00fd thao t\u00e1c v\u1edbi c\u01a1 s\u1edf d\u1eef li\u1ec7u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/AutoBidConfig.java",
    "name": "AutoBidConfig.java",
    "filePath": "src/main/java/org/example/entity/AutoBidConfig.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 \u0111\u1ea1i di\u1ec7n cho c\u1ea5u h\u00ecnh d\u1eef li\u1ec7u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/AuctionSystemException.java",
    "name": "AuctionSystemException.java",
    "filePath": "src/main/java/org/example/exception/AuctionSystemException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auction/AuctionClosedException.java",
    "name": "AuctionClosedException.java",
    "filePath": "src/main/java/org/example/exception/auction/AuctionClosedException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auction/AuctionNotFoundException.java",
    "name": "AuctionNotFoundException.java",
    "filePath": "src/main/java/org/example/exception/auction/AuctionNotFoundException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auction/InvalidAuctionTimeException.java",
    "name": "InvalidAuctionTimeException.java",
    "filePath": "src/main/java/org/example/exception/auction/InvalidAuctionTimeException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auth/DuplicateUsernameException.java",
    "name": "DuplicateUsernameException.java",
    "filePath": "src/main/java/org/example/exception/auth/DuplicateUsernameException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auth/InvalidCredentialsException.java",
    "name": "InvalidCredentialsException.java",
    "filePath": "src/main/java/org/example/exception/auth/InvalidCredentialsException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auth/InvalidRoleException.java",
    "name": "InvalidRoleException.java",
    "filePath": "src/main/java/org/example/exception/auth/InvalidRoleException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auth/NotLoggedInException.java",
    "name": "NotLoggedInException.java",
    "filePath": "src/main/java/org/example/exception/auth/NotLoggedInException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auth/SellersRatingException.java",
    "name": "SellersRatingException.java",
    "filePath": "src/main/java/org/example/exception/auth/SellersRatingException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/auth/UserNotFoundException.java",
    "name": "UserNotFoundException.java",
    "filePath": "src/main/java/org/example/exception/auth/UserNotFoundException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/balance/InvalidTopUpAmountException.java",
    "name": "InvalidTopUpAmountException.java",
    "filePath": "src/main/java/org/example/exception/balance/InvalidTopUpAmountException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/bid/InsufficientBalanceException.java",
    "name": "InsufficientBalanceException.java",
    "filePath": "src/main/java/org/example/exception/bid/InsufficientBalanceException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/bid/InvalidBidException.java",
    "name": "InvalidBidException.java",
    "filePath": "src/main/java/org/example/exception/bid/InvalidBidException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/bid/SelfBidException.java",
    "name": "SelfBidException.java",
    "filePath": "src/main/java/org/example/exception/bid/SelfBidException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/item/InvalidItemNameException.java",
    "name": "InvalidItemNameException.java",
    "filePath": "src/main/java/org/example/exception/item/InvalidItemNameException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/item/InvalidItemPriceException.java",
    "name": "InvalidItemPriceException.java",
    "filePath": "src/main/java/org/example/exception/item/InvalidItemPriceException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/item/InvalidItemStateException.java",
    "name": "InvalidItemStateException.java",
    "filePath": "src/main/java/org/example/exception/item/InvalidItemStateException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/item/ItemNotFoundException.java",
    "name": "ItemNotFoundException.java",
    "filePath": "src/main/java/org/example/exception/item/ItemNotFoundException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/item/UnauthorizedAccessException.java",
    "name": "UnauthorizedAccessException.java",
    "filePath": "src/main/java/org/example/exception/item/UnauthorizedAccessException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/systemANDconcurrency/InvalidRequestException.java",
    "name": "InvalidRequestException.java",
    "filePath": "src/main/java/org/example/exception/systemANDconcurrency/InvalidRequestException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 (Exception) x\u1eed l\u00fd l\u1ed7i nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/AuctionService.java",
    "name": "AuctionService.java",
    "filePath": "src/main/java/org/example/service/AuctionService.java",
    "summary": "L\u1edbp d\u1ecbch v\u1ee5 (Service) trung t\u00e2m ch\u1ee9a logic nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/AutoBidService.java",
    "name": "AutoBidService.java",
    "filePath": "src/main/java/org/example/service/AutoBidService.java",
    "summary": "L\u1edbp d\u1ecbch v\u1ee5 (Service) trung t\u00e2m ch\u1ee9a logic nghi\u1ec7p v\u1ee5.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/service/ItemServiceTest.java",
    "name": "ItemServiceTest.java",
    "filePath": "src/test/java/org/example/service/ItemServiceTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed (Test) \u0111\u1ea3m b\u1ea3o t\u00ednh \u0111\u00fang \u0111\u1eafn c\u1ee7a h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/service/UserServiceTest.java",
    "name": "UserServiceTest.java",
    "filePath": "src/test/java/org/example/service/UserServiceTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed (Test) \u0111\u1ea3m b\u1ea3o t\u00ednh \u0111\u00fang \u0111\u1eafn c\u1ee7a h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/user/UserDAO.java",
    "name": "UserDAO.java",
    "filePath": "src/main/java/org/example/dao/user/UserDAO.java",
    "summary": "L\u1edbp x\u1eed l\u00fd truy c\u1eadp c\u01a1 s\u1edf d\u1eef li\u1ec7u (DAO) cho ng\u01b0\u1eddi d\u00f9ng. Cung c\u1ea5p c\u00e1c thao t\u00e1c CRUD v\u00e0 ki\u1ec3m tra logic \u0111\u0103ng nh\u1eadp.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/user/UserFactory.java",
    "name": "UserFactory.java",
    "filePath": "src/main/java/org/example/dao/user/UserFactory.java",
    "summary": "L\u1edbp Factory gi\u00fap kh\u1edfi t\u1ea1o \u0111\u1ed1i t\u01b0\u1ee3ng ng\u01b0\u1eddi d\u00f9ng t\u1eeb k\u1ebft qu\u1ea3 truy v\u1ea5n c\u01a1 s\u1edf d\u1eef li\u1ec7u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/ChangePasswordRequest.java",
    "name": "ChangePasswordRequest.java",
    "filePath": "src/main/java/org/example/dto/request/ChangePasswordRequest.java",
    "summary": "L\u1edbp DTO \u0111\u00f3ng g\u00f3i d\u1eef li\u1ec7u y\u00eau c\u1ea7u thay \u0111\u1ed5i m\u1eadt kh\u1ea9u t\u1eeb client.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/LoginRequest.java",
    "name": "LoginRequest.java",
    "filePath": "src/main/java/org/example/dto/request/LoginRequest.java",
    "summary": "L\u1edbp DTO \u0111\u00f3ng g\u00f3i d\u1eef li\u1ec7u y\u00eau c\u1ea7u \u0111\u0103ng nh\u1eadp t\u1eeb client.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/RegisterRequest.java",
    "name": "RegisterRequest.java",
    "filePath": "src/main/java/org/example/dto/request/RegisterRequest.java",
    "summary": "L\u1edbp DTO \u0111\u00f3ng g\u00f3i d\u1eef li\u1ec7u y\u00eau c\u1ea7u \u0111\u0103ng k\u00fd t\u00e0i kho\u1ea3n.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/TopUpRequest.java",
    "name": "TopUpRequest.java",
    "filePath": "src/main/java/org/example/dto/request/TopUpRequest.java",
    "summary": "L\u1edbp DTO \u0111\u00f3ng g\u00f3i d\u1eef li\u1ec7u y\u00eau c\u1ea7u n\u1ea1p ti\u1ec1n v\u00e0o t\u00e0i kho\u1ea3n.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/BaseResponse.java",
    "name": "BaseResponse.java",
    "filePath": "src/main/java/org/example/dto/response/BaseResponse.java",
    "summary": "L\u1edbp c\u01a1 s\u1edf \u0111\u00f3ng g\u00f3i ph\u1ea3n h\u1ed3i ti\u00eau chu\u1ea9n cho c\u00e1c y\u00eau c\u1ea7u m\u1ea1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/SimpleResponse.java",
    "name": "SimpleResponse.java",
    "filePath": "src/main/java/org/example/dto/response/SimpleResponse.java",
    "summary": "L\u1edbp DTO \u0111\u00f3ng g\u00f3i c\u00e1c ph\u1ea3n h\u1ed3i \u0111\u01a1n gi\u1ea3n (th\u00e0nh c\u00f4ng, l\u1ed7i, th\u00f4ng tin).",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/TopUpResponse.java",
    "name": "TopUpResponse.java",
    "filePath": "src/main/java/org/example/dto/response/TopUpResponse.java",
    "summary": "L\u1edbp DTO \u0111\u00f3ng g\u00f3i ph\u1ea3n h\u1ed3i cho y\u00eau c\u1ea7u n\u1ea1p ti\u1ec1n.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/BaseEntity.java",
    "name": "BaseEntity.java",
    "filePath": "src/main/java/org/example/entity/BaseEntity.java",
    "summary": "Th\u1ef1c th\u1ec3 c\u01a1 s\u1edf cung c\u1ea5p ID chung cho c\u00e1c entity.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/user/Admin.java",
    "name": "Admin.java",
    "filePath": "src/main/java/org/example/entity/user/Admin.java",
    "summary": "Th\u1ef1c th\u1ec3 \u0111\u1ea1i di\u1ec7n cho ng\u01b0\u1eddi d\u00f9ng c\u00f3 quy\u1ec1n qu\u1ea3n tr\u1ecb (Admin).",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/user/Bidder.java",
    "name": "Bidder.java",
    "filePath": "src/main/java/org/example/entity/user/Bidder.java",
    "summary": "Th\u1ef1c th\u1ec3 \u0111\u1ea1i di\u1ec7n cho ng\u01b0\u1eddi \u0111\u1ea5u gi\u00e1 (Bidder).",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/user/Seller.java",
    "name": "Seller.java",
    "filePath": "src/main/java/org/example/entity/user/Seller.java",
    "summary": "Th\u1ef1c th\u1ec3 \u0111\u1ea1i di\u1ec7n cho ng\u01b0\u1eddi b\u00e1n (Seller).",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/user/User.java",
    "name": "User.java",
    "filePath": "src/main/java/org/example/entity/user/User.java",
    "summary": "Th\u1ef1c th\u1ec3 c\u01a1 s\u1edf cho t\u1ea5t c\u1ea3 ng\u01b0\u1eddi d\u00f9ng trong h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/CommandRouter.java",
    "name": "CommandRouter.java",
    "filePath": "src/main/java/org/example/network/CommandRouter.java",
    "summary": "Th\u00e0nh ph\u1ea7n \u0111i\u1ec1u h\u01b0\u1edbng (Router) cho c\u00e1c y\u00eau c\u1ea7u m\u1ea1ng t\u1eeb client. Ti\u1ebfp nh\u1eadn v\u00e0 g\u1ecdi t\u1edbi c\u00e1c Controller t\u01b0\u01a1ng \u1ee9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/SessionContext.java",
    "name": "SessionContext.java",
    "filePath": "src/main/java/org/example/network/SessionContext.java",
    "summary": "L\u1edbp ti\u1ec7n \u00edch qu\u1ea3n l\u00fd tr\u1ea1ng th\u00e1i phi\u00ean l\u00e0m vi\u1ec7c (Session) c\u1ee7a client k\u1ebft n\u1ed1i.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/controller/AuthController.java",
    "name": "AuthController.java",
    "filePath": "src/main/java/org/example/network/controller/AuthController.java",
    "summary": "Th\u00e0nh ph\u1ea7n \u0111i\u1ec1u h\u01b0\u1edbng (Controller) x\u1eed l\u00fd x\u00e1c th\u1ef1c ng\u01b0\u1eddi d\u00f9ng bao g\u1ed3m \u0111\u0103ng nh\u1eadp, \u0111\u0103ng k\u00fd v\u00e0 \u0111\u1ed5i m\u1eadt kh\u1ea9u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/controller/UserController.java",
    "name": "UserController.java",
    "filePath": "src/main/java/org/example/network/controller/UserController.java",
    "summary": "Th\u00e0nh ph\u1ea7n \u0111i\u1ec1u h\u01b0\u1edbng (Controller) cho c\u00e1c nghi\u1ec7p v\u1ee5 ng\u01b0\u1eddi d\u00f9ng, v\u00ed d\u1ee5 nh\u01b0 n\u1ea1p ti\u1ec1n v\u00e0 truy xu\u1ea5t ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/UserService.java",
    "name": "UserService.java",
    "filePath": "src/main/java/org/example/service/UserService.java",
    "summary": "L\u1edbp d\u1ecbch v\u1ee5 (Service) x\u1eed l\u00fd to\u00e0n b\u1ed9 logic nghi\u1ec7p v\u1ee5 c\u1ed1t l\u00f5i v\u1ec1 ng\u01b0\u1eddi d\u00f9ng nh\u01b0 \u0111\u0103ng k\u00fd, \u0111\u0103ng nh\u1eadp v\u00e0 s\u1ed1 d\u01b0.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/dao/user/UserFactoryTest.java",
    "name": "UserFactoryTest.java",
    "filePath": "src/test/java/org/example/dao/user/UserFactoryTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed (Test) \u0111\u1ea3m b\u1ea3o ho\u1ea1t \u0111\u1ed9ng c\u1ee7a UserFactory khi kh\u1edfi t\u1ea1o \u0111\u1ed1i t\u01b0\u1ee3ng t\u1eeb ResultSet.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/network/ClientHandlerTest.java",
    "name": "ClientHandlerTest.java",
    "filePath": "src/test/java/org/example/network/ClientHandlerTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed (Test) cho k\u1ebft n\u1ed1i m\u1ea1ng ClientHandler.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/testutil/ResultSetStub.java",
    "name": "ResultSetStub.java",
    "filePath": "src/test/java/org/example/testutil/ResultSetStub.java",
    "summary": "L\u1edbp ti\u1ec7n \u00edch mock \u0111\u1ed1i t\u01b0\u1ee3ng ResultSet \u0111\u1ec3 h\u1ed7 tr\u1ee3 ki\u1ec3m th\u1eed kh\u00f4ng c\u1ea7n DB th\u1eadt.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/ServerMain.java",
    "name": "ServerMain.java",
    "filePath": "src/main/java/org/example/ServerMain.java",
    "summary": "\u0110i\u1ec3m v\u00e0o ch\u00ednh (entry point) c\u1ee7a \u1ee9ng d\u1ee5ng m\u00e1y ch\u1ee7, ch\u1ecbu tr\u00e1ch nhi\u1ec7m kh\u1edfi t\u1ea1o c\u00e1c d\u1ecbch v\u1ee5 c\u1ed1t l\u00f5i v\u00e0 kh\u1edfi \u0111\u1ed9ng server.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/AuctionDAO.java",
    "name": "AuctionDAO.java",
    "filePath": "src/main/java/org/example/dao/AuctionDAO.java",
    "summary": "L\u1edbp t\u01b0\u01a1ng t\u00e1c v\u1edbi c\u01a1 s\u1edf d\u1eef li\u1ec7u \u0111\u1ec3 th\u1ef1c hi\u1ec7n c\u00e1c thao t\u00e1c th\u00eam, s\u1eeda, \u0111\u1ecdc tr\u1ea1ng th\u00e1i phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/BidResult.java",
    "name": "BidResult.java",
    "filePath": "src/main/java/org/example/dto/BidResult.java",
    "summary": "Data Transfer Object (DTO) ch\u1ee9a k\u1ebft qu\u1ea3 c\u1ee7a m\u1ed9t l\u01b0\u1ee3t tr\u1ea3 gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/AutoBidRequest.java",
    "name": "AutoBidRequest.java",
    "filePath": "src/main/java/org/example/dto/request/AutoBidRequest.java",
    "summary": "DTO \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac y\u00eau c\u1ea7u thi\u1ebft l\u1eadp tr\u1ea3 gi\u00e1 t\u1ef1 \u0111\u1ed9ng t\u1eeb ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/BidRequest.java",
    "name": "BidRequest.java",
    "filePath": "src/main/java/org/example/dto/request/BidRequest.java",
    "summary": "DTO \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac y\u00eau c\u1ea7u tr\u1ea3 gi\u00e1 th\u00f4ng th\u01b0\u1eddng t\u1eeb ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AutoBidResponse.java",
    "name": "AutoBidResponse.java",
    "filePath": "src/main/java/org/example/dto/response/AutoBidResponse.java",
    "summary": "DTO \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac ph\u1ea3n h\u1ed3i h\u1ec7 th\u1ed1ng sau khi nh\u1eadn y\u00eau c\u1ea7u tr\u1ea3 gi\u00e1 t\u1ef1 \u0111\u1ed9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/BidResponse.java",
    "name": "BidResponse.java",
    "filePath": "src/main/java/org/example/dto/response/BidResponse.java",
    "summary": "DTO \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac ph\u1ea3n h\u1ed3i k\u1ebft qu\u1ea3 tr\u1ea3 gi\u00e1 cho client.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/Auction.java",
    "name": "Auction.java",
    "filePath": "src/main/java/org/example/entity/Auction.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 (Entity) \u0111\u1ea1i di\u1ec7n cho m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1, ch\u1ee9a th\u00f4ng tin v\u1ec1 s\u1ea3n ph\u1ea9m, gi\u00e1 c\u1ea3 v\u00e0 tr\u1ea1ng th\u00e1i.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/systemANDconcurrency/ConcurrentBidException.java",
    "name": "ConcurrentBidException.java",
    "filePath": "src/main/java/org/example/exception/systemANDconcurrency/ConcurrentBidException.java",
    "summary": "Ngo\u1ea1i l\u1ec7 t\u00f9y ch\u1ec9nh \u0111\u1ec3 x\u1eed l\u00fd l\u1ed7i \u0111\u1ed3ng th\u1eddi khi nhi\u1ec1u ng\u01b0\u1eddi d\u00f9ng c\u00f9ng tr\u1ea3 gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/AuctionServer.java",
    "name": "AuctionServer.java",
    "filePath": "src/main/java/org/example/network/AuctionServer.java",
    "summary": "L\u1edbp qu\u1ea3n l\u00fd vi\u1ec7c k\u1ebft n\u1ed1i m\u1ea1ng v\u00e0 l\u1eafng nghe y\u00eau c\u1ea7u t\u1eeb c\u00e1c client th\u00f4ng qua socket.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/ClientHandler.java",
    "name": "ClientHandler.java",
    "filePath": "src/main/java/org/example/network/ClientHandler.java",
    "summary": "X\u1eed l\u00fd t\u1eebng k\u1ebft n\u1ed1i client \u0111\u1ed9c l\u1eadp, ph\u00e2n ph\u1ed1i c\u00e1c y\u00eau c\u1ea7u t\u1edbi c\u00e1c controller t\u01b0\u01a1ng \u1ee9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/controller/BidController.java",
    "name": "BidController.java",
    "filePath": "src/main/java/org/example/network/controller/BidController.java",
    "summary": "Controller ti\u1ebfp nh\u1eadn v\u00e0 x\u1eed l\u00fd c\u00e1c nghi\u1ec7p v\u1ee5 li\u00ean quan \u0111\u1ebfn tr\u1ea3 gi\u00e1 v\u00e0 tr\u1ea3 gi\u00e1 t\u1ef1 \u0111\u1ed9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/observer/BidObserver.java",
    "name": "BidObserver.java",
    "filePath": "src/main/java/org/example/observer/BidObserver.java",
    "summary": "Interface \u00e1p d\u1ee5ng m\u1eabu thi\u1ebft k\u1ebf Observer \u0111\u1ec3 theo d\u00f5i v\u00e0 nh\u1eadn th\u00f4ng b\u00e1o v\u1ec1 c\u00e1c thay \u0111\u1ed5i tr\u1ea3 gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/AuctionEngine.java",
    "name": "AuctionEngine.java",
    "filePath": "src/main/java/org/example/service/AuctionEngine.java",
    "summary": "Engine c\u1ed1t l\u00f5i qu\u1ea3n l\u00fd to\u00e0n b\u1ed9 v\u00f2ng \u0111\u1eddi c\u1ee7a c\u00e1c phi\u00ean \u0111\u1ea5u gi\u00e1 v\u00e0 x\u1eed l\u00fd logic nghi\u1ec7p v\u1ee5 \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/AuctionRoom.java",
    "name": "AuctionRoom.java",
    "filePath": "src/main/java/org/example/service/AuctionRoom.java",
    "summary": "\u0110\u1ea1i di\u1ec7n cho m\u1ed9t ph\u00f2ng \u0111\u1ea5u gi\u00e1, qu\u1ea3n l\u00fd nh\u1eefng ng\u01b0\u1eddi tham gia (observer) v\u00e0 ph\u00e1t th\u00f4ng b\u00e1o khi c\u00f3 tr\u1ea3 gi\u00e1 m\u1edbi.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/AuctionRoomManager.java",
    "name": "AuctionRoomManager.java",
    "filePath": "src/main/java/org/example/service/AuctionRoomManager.java",
    "summary": "Qu\u1ea3n l\u00fd danh s\u00e1ch v\u00e0 v\u00f2ng \u0111\u1eddi c\u1ee7a c\u00e1c ph\u00f2ng \u0111\u1ea5u gi\u00e1 (AuctionRoom) \u0111ang ho\u1ea1t \u0111\u1ed9ng.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/AuctionEngineTest.java",
    "name": "AuctionEngineTest.java",
    "filePath": "src/test/java/org/example/AuctionEngineTest.java",
    "summary": "L\u1edbp ch\u1ee9a c\u00e1c b\u00e0i ki\u1ec3m th\u1eed (unit tests) cho AuctionEngine \u0111\u1ec3 \u0111\u1ea3m b\u1ea3o logic \u0111\u1ea5u gi\u00e1 ho\u1ea1t \u0111\u1ed9ng ch\u00ednh x\u00e1c.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/item/ItemDAO.java",
    "name": "ItemDAO.java",
    "filePath": "src/main/java/org/example/dao/item/ItemDAO.java",
    "summary": "L\u1edbp DAO x\u1eed l\u00fd c\u00e1c thao t\u00e1c CRUD v\u1edbi c\u01a1 s\u1edf d\u1eef li\u1ec7u cho th\u1ef1c th\u1ec3 Item.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/GetAuctionDetailRequest.java",
    "name": "GetAuctionDetailRequest.java",
    "filePath": "src/main/java/org/example/dto/request/GetAuctionDetailRequest.java",
    "summary": "L\u1edbp DTO \u0111\u1ea1i di\u1ec7n cho y\u00eau c\u1ea7u l\u1ea5y th\u00f4ng tin chi ti\u1ebft c\u1ee7a m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/GetBidHistoryRequest.java",
    "name": "GetBidHistoryRequest.java",
    "filePath": "src/main/java/org/example/dto/request/GetBidHistoryRequest.java",
    "summary": "L\u1edbp DTO ch\u1ee9a d\u1eef li\u1ec7u y\u00eau c\u1ea7u \u0111\u1ec3 truy xu\u1ea5t l\u1ecbch s\u1eed \u0111\u1eb7t gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/JoinRequest.java",
    "name": "JoinRequest.java",
    "filePath": "src/main/java/org/example/dto/request/JoinRequest.java",
    "summary": "L\u1edbp DTO \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac y\u00eau c\u1ea7u tham gia v\u00e0o phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/BidHistory.java",
    "name": "BidHistory.java",
    "filePath": "src/main/java/org/example/entity/BidHistory.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 m\u00f4 h\u00ecnh h\u00f3a l\u1ecbch s\u1eed \u0111\u1eb7t gi\u00e1 c\u1ee7a ng\u01b0\u1eddi d\u00f9ng trong h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/item/Art.java",
    "name": "Art.java",
    "filePath": "src/main/java/org/example/entity/item/Art.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 k\u1ebf th\u1eeba t\u1eeb Item, \u0111\u1ea1i di\u1ec7n cho m\u1eb7t h\u00e0ng ngh\u1ec7 thu\u1eadt.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/item/Electronics.java",
    "name": "Electronics.java",
    "filePath": "src/main/java/org/example/entity/item/Electronics.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 k\u1ebf th\u1eeba t\u1eeb Item, \u0111\u1ea1i di\u1ec7n cho m\u1eb7t h\u00e0ng \u0111i\u1ec7n t\u1eed.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/item/Item.java",
    "name": "Item.java",
    "filePath": "src/main/java/org/example/entity/item/Item.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 c\u01a1 s\u1edf \u0111\u1ecbnh ngh\u0129a c\u00e1c thu\u1ed9c t\u00ednh chung cho c\u00e1c m\u1eb7t h\u00e0ng \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/item/Vehicle.java",
    "name": "Vehicle.java",
    "filePath": "src/main/java/org/example/entity/item/Vehicle.java",
    "summary": "L\u1edbp th\u1ef1c th\u1ec3 k\u1ebf th\u1eeba t\u1eeb Item, \u0111\u1ea1i di\u1ec7n cho ph\u01b0\u01a1ng ti\u1ec7n giao th\u00f4ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/controller/AuctionController.java",
    "name": "AuctionController.java",
    "filePath": "src/main/java/org/example/network/controller/AuctionController.java",
    "summary": "B\u1ed9 \u0111i\u1ec1u khi\u1ec3n x\u1eed l\u00fd c\u00e1c y\u00eau c\u1ea7u m\u1ea1ng li\u00ean quan \u0111\u1ebfn lu\u1ed3ng \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/network/controller/ItemController.java",
    "name": "ItemController.java",
    "filePath": "src/main/java/org/example/network/controller/ItemController.java",
    "summary": "B\u1ed9 \u0111i\u1ec1u khi\u1ec3n ti\u1ebfp nh\u1eadn v\u00e0 x\u1eed l\u00fd c\u00e1c thao t\u00e1c qu\u1ea3n l\u00fd m\u1eb7t h\u00e0ng qua m\u1ea1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/service/ItemService.java",
    "name": "ItemService.java",
    "filePath": "src/main/java/org/example/service/ItemService.java",
    "summary": "L\u1edbp d\u1ecbch v\u1ee5 ch\u1ee9a logic nghi\u1ec7p v\u1ee5 c\u1ed1t l\u00f5i \u0111\u1ec3 qu\u1ea3n l\u00fd c\u00e1c m\u1eb7t h\u00e0ng trong h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/dao/item/ItemFactoryTest.java",
    "name": "ItemFactoryTest.java",
    "filePath": "src/test/java/org/example/dao/item/ItemFactoryTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed \u0111\u01a1n v\u1ecb \u0111\u1ea3m b\u1ea3o t\u00ednh \u0111\u00fang \u0111\u1eafn c\u1ee7a vi\u1ec7c kh\u1edfi t\u1ea1o c\u00e1c m\u1eb7t h\u00e0ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/AuctionDetailController.java",
    "name": "AuctionDetailController.java",
    "filePath": "src/main/java/com/auction/client/controllers/AuctionDetailController.java",
    "summary": "T\u1ec7p Controller qu\u1ea3n l\u00fd giao di\u1ec7n chi ti\u1ebft c\u1ee7a m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1, x\u1eed l\u00fd hi\u1ec3n th\u1ecb th\u00f4ng tin, l\u1ecbch s\u1eed \u0111\u1eb7t gi\u00e1 v\u00e0 \u0111\u1ed3ng h\u1ed3 \u0111\u1ebfm ng\u01b0\u1ee3c.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/ConnectedLoginController.java",
    "name": "ConnectedLoginController.java",
    "filePath": "src/main/java/com/auction/client/controllers/ConnectedLoginController.java",
    "summary": "Controller x\u1eed l\u00fd giao di\u1ec7n \u0111\u0103ng nh\u1eadp v\u00e0 \u0111\u0103ng k\u00fd khi h\u1ec7 th\u1ed1ng \u0111\u00e3 c\u00f3 k\u1ebft n\u1ed1i v\u1edbi m\u00e1y ch\u1ee7.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/DashboardController.java",
    "name": "DashboardController.java",
    "filePath": "src/main/java/com/auction/client/controllers/DashboardController.java",
    "summary": "Controller cho trang t\u1ed5ng quan (dashboard), hi\u1ec3n th\u1ecb danh s\u00e1ch c\u00e1c s\u1ea3n ph\u1ea9m \u0111ang \u0111\u01b0\u1ee3c \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/DepositController.java",
    "name": "DepositController.java",
    "filePath": "src/main/java/com/auction/client/controllers/DepositController.java",
    "summary": "Controller x\u1eed l\u00fd ch\u1ee9c n\u0103ng n\u1ea1p ti\u1ec1n v\u00e0o t\u00e0i kho\u1ea3n ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/LoginController.java",
    "name": "LoginController.java",
    "filePath": "src/main/java/com/auction/client/controllers/LoginController.java",
    "summary": "Controller ch\u00ednh qu\u1ea3n l\u00fd m\u00e0n h\u00ecnh \u0111\u0103ng nh\u1eadp, th\u1ef1c hi\u1ec7n vi\u1ec7c kh\u1edfi t\u1ea1o k\u1ebft n\u1ed1i v\u00e0 x\u00e1c th\u1ef1c ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/MainController.java",
    "name": "MainController.java",
    "filePath": "src/main/java/com/auction/client/controllers/MainController.java",
    "summary": "Controller c\u1ea5p cao nh\u1ea5t qu\u1ea3n l\u00fd b\u1ed1 c\u1ee5c t\u1ed5ng th\u1ec3 nh\u01b0 menu \u0111i\u1ec1u h\u01b0\u1edbng v\u00e0 v\u00f9ng n\u1ed9i dung thay \u0111\u1ed5i linh ho\u1ea1t.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/ProductManagementController.java",
    "name": "ProductManagementController.java",
    "filePath": "src/main/java/com/auction/client/controllers/ProductManagementController.java",
    "summary": "T\u1ec7p Controller qu\u1ea3n l\u00fd danh s\u00e1ch s\u1ea3n ph\u1ea9m c\u1ee7a ng\u01b0\u1eddi b\u00e1n, cho ph\u00e9p t\u1ea3i d\u1eef li\u1ec7u v\u00e0 c\u1eadp nh\u1eadt th\u00f4ng tin s\u1ea3n ph\u1ea9m.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/RegisterController.java",
    "name": "RegisterController.java",
    "filePath": "src/main/java/com/auction/client/controllers/RegisterController.java",
    "summary": "Controller x\u1eed l\u00fd giao di\u1ec7n \u0111\u0103ng k\u00fd t\u00e0i kho\u1ea3n m\u1edbi cho ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/network/ConnectionManager.java",
    "name": "ConnectionManager.java",
    "filePath": "src/main/java/com/auction/client/network/ConnectionManager.java",
    "summary": "Qu\u1ea3n l\u00fd k\u1ebft n\u1ed1i socket t\u1edbi m\u00e1y ch\u1ee7, duy tr\u00ec tr\u1ea1ng th\u00e1i k\u1ebft n\u1ed1i v\u00e0 x\u1eed l\u00fd lu\u1ed3ng th\u00f4ng \u0111i\u1ec7p b\u1ea5t \u0111\u1ed3ng b\u1ed9.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/network/ServerClient.java",
    "name": "ServerClient.java",
    "filePath": "src/main/java/com/auction/client/network/ServerClient.java",
    "summary": "Ti\u1ec7n \u00edch g\u1eedi y\u00eau c\u1ea7u m\u1ea1ng \u0111\u1ed3ng b\u1ed9 theo t\u1eebng phi\u00ean giao d\u1ecbch \u0111\u1ed9c l\u1eadp t\u1edbi m\u00e1y ch\u1ee7.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/utils/ConfigManager.java",
    "name": "ConfigManager.java",
    "filePath": "src/main/java/org/example/utils/ConfigManager.java",
    "summary": "C\u00f4ng c\u1ee5 qu\u1ea3n l\u00fd c\u1ea5u h\u00ecnh (properties) h\u1ed7 tr\u1ee3 t\u1ea3i d\u1eef li\u1ec7u t\u1eeb t\u1ec7p c\u1ee5c b\u1ed9 ho\u1eb7c t\u1eeb classpath.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/BidHistoryDAO.java",
    "name": "BidHistoryDAO.java",
    "filePath": "src/main/java/org/example/dao/BidHistoryDAO.java",
    "summary": "Cung c\u1ea5p c\u00e1c ph\u01b0\u01a1ng th\u1ee9c t\u01b0\u01a1ng t\u00e1c v\u1edbi c\u01a1 s\u1edf d\u1eef li\u1ec7u cho l\u1ecbch s\u1eed \u0111\u1ea5u gi\u00e1, bao g\u1ed3m th\u00eam m\u1edbi, truy v\u1ea5n, \u0111\u1ebfm s\u1ed1 l\u01b0\u1ee3ng v\u00e0 x\u00f3a l\u1ecbch s\u1eed \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/BidTransactionDAO.java",
    "name": "BidTransactionDAO.java",
    "filePath": "src/main/java/org/example/dao/BidTransactionDAO.java",
    "summary": "X\u1eed l\u00fd c\u00e1c thao t\u00e1c truy xu\u1ea5t v\u00e0 l\u01b0u tr\u1eef d\u1eef li\u1ec7u li\u00ean quan \u0111\u1ebfn giao d\u1ecbch \u0111\u1ea5u gi\u00e1 v\u00e0o c\u01a1 s\u1edf d\u1eef li\u1ec7u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/entity/BidTransaction.java",
    "name": "BidTransaction.java",
    "filePath": "src/main/java/org/example/entity/BidTransaction.java",
    "summary": "\u0110\u1ecbnh ngh\u0129a th\u1ef1c th\u1ec3 giao d\u1ecbch \u0111\u1ea5u gi\u00e1 v\u1edbi c\u00e1c thu\u1ed9c t\u00ednh nh\u01b0 ID phi\u00ean \u0111\u1ea5u gi\u00e1, ng\u01b0\u1eddi \u0111\u1eb7t gi\u00e1, gi\u00e1 th\u1ea7u v\u00e0 th\u1eddi gian \u0111\u1eb7t.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/exception/database/DatabaseException.java",
    "name": "DatabaseException.java",
    "filePath": "src/main/java/org/example/exception/database/DatabaseException.java",
    "summary": "L\u1edbp ngo\u1ea1i l\u1ec7 t\u00f9y ch\u1ec9nh \u0111\u1ec3 x\u1eed l\u00fd c\u00e1c l\u1ed7i li\u00ean quan \u0111\u1ebfn thao t\u00e1c v\u1edbi c\u01a1 s\u1edf d\u1eef li\u1ec7u trong h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/utils/DatabaseConnection.java",
    "name": "DatabaseConnection.java",
    "filePath": "src/main/java/org/example/utils/DatabaseConnection.java",
    "summary": "L\u1edbp ti\u1ec7n \u00edch qu\u1ea3n l\u00fd k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u s\u1eed d\u1ee5ng m\u1eabu Singleton, cung c\u1ea5p k\u1ebft n\u1ed1i JDBC d\u00f9ng chung cho to\u00e0n \u1ee9ng d\u1ee5ng.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/dao/AuctionDAOTest.java",
    "name": "AuctionDAOTest.java",
    "filePath": "src/test/java/org/example/dao/AuctionDAOTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed \u0111\u01a1n v\u1ecb cho AuctionDAO, ki\u1ec3m tra logic c\u1eadp nh\u1eadt gi\u00e1 hi\u1ec7n t\u1ea1i s\u1eed d\u1ee5ng k\u1ebft n\u1ed1i gi\u1ea3 (mock connection).",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/dao/BidTransactionDAOTest.java",
    "name": "BidTransactionDAOTest.java",
    "filePath": "src/test/java/org/example/dao/BidTransactionDAOTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed \u0111\u01a1n v\u1ecb cho BidTransactionDAO, ki\u1ec3m th\u1eed ch\u1ee9c n\u0103ng th\u00eam m\u1edbi giao d\u1ecbch gi\u00e1 th\u1ea7u v\u1edbi c\u01a1 s\u1edf d\u1eef li\u1ec7u.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/dao/user/UserDAOTest.java",
    "name": "UserDAOTest.java",
    "filePath": "src/test/java/org/example/dao/user/UserDAOTest.java",
    "summary": "L\u1edbp ki\u1ec3m th\u1eed \u0111\u01a1n v\u1ecb cho UserDAO, t\u1eadp trung v\u00e0o ch\u1ee9c n\u0103ng c\u1eadp nh\u1eadt s\u1ed1 d\u01b0 c\u1ee7a ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/testutil/SqlTestSupport.java",
    "name": "SqlTestSupport.java",
    "filePath": "src/test/java/org/example/testutil/SqlTestSupport.java",
    "summary": "L\u1edbp ti\u1ec7n \u00edch h\u1ed7 tr\u1ee3 ki\u1ec3m th\u1eed SQL, cung c\u1ea5p k\u1ebft n\u1ed1i gi\u1ea3 \u0111\u1ec3 ghi l\u1ea1i c\u00e1c c\u00e2u l\u1ec7nh SQL v\u00e0 tham s\u1ed1 \u0111\u01b0\u1ee3c th\u1ef1c thi.",
    "type": "file"
  },
  {
    "id": "pipeline:.github/workflows/backend-ci.yml",
    "name": ".github/workflows/backend-ci.yml",
    "filePath": ".github/workflows/backend-ci.yml",
    "summary": "Quy tr\u00ecnh CI/CD s\u1eed d\u1ee5ng GitHub Actions cho backend, t\u1ef1 \u0111\u1ed9ng thi\u1ebft l\u1eadp m\u00f4i tr\u01b0\u1eddng JDK, c\u1ea5u h\u00ecnh c\u01a1 s\u1edf d\u1eef li\u1ec7u MySQL v\u00e0 ch\u1ea1y Maven \u0111\u1ec3 ki\u1ec3m th\u1eed, \u0111\u00f3ng g\u00f3i \u1ee9ng d\u1ee5ng.",
    "type": "pipeline"
  },
  {
    "id": "config:application.properties",
    "name": "application.properties",
    "filePath": "application.properties",
    "summary": "T\u1ec7p c\u1ea5u h\u00ecnh ch\u1ee9a c\u00e1c thi\u1ebft l\u1eadp quan tr\u1ecdng cho \u1ee9ng d\u1ee5ng nh\u01b0 chu\u1ed7i k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u MySQL, th\u00f4ng s\u1ed1 m\u00e1y ch\u1ee7 v\u00e0 c\u00e1c quy t\u1eafc \u0111\u1ea5u gi\u00e1, bao g\u1ed3m c\u1ea3 t\u00ednh n\u0103ng ch\u1ed1ng b\u1eafn t\u1ec9a (anti-sniping).",
    "type": "config"
  },
  {
    "id": "config:pom.xml",
    "name": "pom.xml",
    "filePath": "pom.xml",
    "summary": "T\u1ec7p c\u1ea5u h\u00ecnh Maven \u0111\u1ecbnh ngh\u0129a c\u00e1c th\u01b0 vi\u1ec7n ph\u1ee5 thu\u1ed9c c\u1ee7a d\u1ef1 \u00e1n (nh\u01b0 JavaFX, MySQL, Gson, BCrypt) v\u00e0 thi\u1ebft l\u1eadp qu\u00e1 tr\u00ecnh build \u0111\u1ec3 t\u1ea1o ra t\u1ec7p thi h\u00e0nh JAR (fat jar) th\u00f4ng qua maven-shade-plugin.",
    "type": "config"
  },
  {
    "id": "document:sources.txt",
    "name": "sources.txt",
    "filePath": "sources.txt",
    "summary": "T\u1ec7p v\u0103n b\u1ea3n ch\u1ee9a danh s\u00e1ch c\u00e1c \u0111\u01b0\u1eddng d\u1eabn t\u0129nh tr\u1ecf \u0111\u1ebfn m\u00e3 ngu\u1ed3n ph\u00eda client c\u1ee7a d\u1ef1 \u00e1n, c\u00f3 th\u1ec3 \u0111\u01b0\u1ee3c d\u00f9ng cho vi\u1ec7c bi\u00ean d\u1ecbch h\u00e0ng lo\u1ea1t ho\u1eb7c tham chi\u1ebfu danh s\u00e1ch t\u1ec7p.",
    "type": "document"
  },
  {
    "id": "file:.understand-anything/.understandignore",
    "name": ".understandignore",
    "filePath": ".understand-anything/.understandignore",
    "summary": "T\u1ec7p c\u1ea5u h\u00ecnh ch\u1ee9a c\u00e1c quy t\u1eafc b\u1ecf qua c\u00e1c th\u01b0 m\u1ee5c ho\u1eb7c t\u1ec7p kh\u00f4ng c\u1ea7n ph\u00e2n t\u00edch.",
    "type": "file"
  },
  {
    "id": "config:.understand-anything/config.json",
    "name": "config.json",
    "filePath": ".understand-anything/config.json",
    "summary": "T\u1ec7p c\u1ea5u h\u00ecnh d\u1ea1ng JSON d\u00e0nh cho c\u00f4ng c\u1ee5 ph\u00e2n t\u00edch Understand Anything.",
    "type": "config"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/BidHistoryController.java",
    "name": "BidHistoryController.java",
    "filePath": "src/main/java/com/auction/client/controllers/BidHistoryController.java",
    "summary": "B\u1ed9 \u0111i\u1ec1u khi\u1ec3n qu\u1ea3n l\u00fd giao di\u1ec7n hi\u1ec3n th\u1ecb l\u1ecbch s\u1eed \u0111\u1ea5u gi\u00e1 cho ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/controllers/UserManagementController.java",
    "name": "UserManagementController.java",
    "filePath": "src/main/java/com/auction/client/controllers/UserManagementController.java",
    "summary": "B\u1ed9 \u0111i\u1ec1u khi\u1ec3n qu\u1ea3n l\u00fd giao di\u1ec7n qu\u1ea3n tr\u1ecb ng\u01b0\u1eddi d\u00f9ng, bao g\u1ed3m t\u1ea3i v\u00e0 c\u1eadp nh\u1eadt tr\u1ea1ng th\u00e1i.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/Launcher.java",
    "name": "Launcher.java",
    "filePath": "src/main/java/com/auction/client/Launcher.java",
    "summary": "T\u1ec7p \u0111\u00f3ng vai tr\u00f2 l\u00e0 \u0111i\u1ec3m v\u00e0o gi\u1ea3 (wrapper) \u0111\u1ec3 kh\u1edfi ch\u1ea1y \u1ee9ng d\u1ee5ng JavaFX.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/Main.java",
    "name": "Main.java",
    "filePath": "src/main/java/com/auction/client/Main.java",
    "summary": "\u0110i\u1ec3m v\u00e0o ch\u00ednh c\u1ee7a \u1ee9ng d\u1ee5ng client JavaFX, thi\u1ebft l\u1eadp giao di\u1ec7n v\u00e0 m\u00e0n h\u00ecnh ch\u00ednh.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/Message.java",
    "name": "Message.java",
    "filePath": "src/main/java/com/auction/client/Message.java",
    "summary": "L\u1edbp \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac g\u00f3i tin giao ti\u1ebfp gi\u1eefa client v\u00e0 server.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/com/auction/client/SocketClient.java",
    "name": "SocketClient.java",
    "filePath": "src/main/java/com/auction/client/SocketClient.java",
    "summary": "Th\u00e0nh ph\u1ea7n c\u1ed1t l\u00f5i qu\u1ea3n l\u00fd k\u1ebft n\u1ed1i socket t\u1edbi server v\u00e0 x\u1eed l\u00fd truy\u1ec1n/nh\u1eadn g\u00f3i tin.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dao/item/ItemFactory.java",
    "name": "ItemFactory.java",
    "filePath": "src/main/java/org/example/dao/item/ItemFactory.java",
    "summary": "M\u1eabu thi\u1ebft k\u1ebf Factory \u0111\u1ec3 kh\u1edfi t\u1ea1o c\u00e1c \u0111\u1ed1i t\u01b0\u1ee3ng Item c\u1ee5 th\u1ec3 t\u1eeb k\u1ebft qu\u1ea3 truy v\u1ea5n c\u01a1 s\u1edf d\u1eef li\u1ec7u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/BaseRequest.java",
    "name": "BaseRequest.java",
    "filePath": "src/main/java/org/example/dto/request/BaseRequest.java",
    "summary": "L\u1edbp c\u01a1 s\u1edf cho c\u00e1c \u0111\u1ed1i t\u01b0\u1ee3ng y\u00eau c\u1ea7u (request) ch\u1ee9a tr\u01b0\u1eddng l\u1ec7nh (command).",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/request/CreateAuctionRequest.java",
    "name": "CreateAuctionRequest.java",
    "filePath": "src/main/java/org/example/dto/request/CreateAuctionRequest.java",
    "summary": "DTO mang d\u1eef li\u1ec7u y\u00eau c\u1ea7u t\u1ea1o m\u1ed9t cu\u1ed9c \u0111\u1ea5u gi\u00e1 m\u1edbi.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AuctionDetailResponse.java",
    "name": "AuctionDetailResponse.java",
    "filePath": "src/main/java/org/example/dto/response/AuctionDetailResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i ch\u1ee9a th\u00f4ng tin chi ti\u1ebft v\u1ec1 m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AuctionExtendedResponse.java",
    "name": "AuctionExtendedResponse.java",
    "filePath": "src/main/java/org/example/dto/response/AuctionExtendedResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i b\u00e1o hi\u1ec7u m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1 \u0111\u00e3 \u0111\u01b0\u1ee3c gia h\u1ea1n th\u1eddi gian.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AuctionListResponse.java",
    "name": "AuctionListResponse.java",
    "filePath": "src/main/java/org/example/dto/response/AuctionListResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i ch\u1ee9a danh s\u00e1ch c\u00e1c cu\u1ed9c \u0111\u1ea5u gi\u00e1 hi\u1ec7n c\u00f3.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AuctionResultResponse.java",
    "name": "AuctionResultResponse.java",
    "filePath": "src/main/java/org/example/dto/response/AuctionResultResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i c\u00f4ng b\u1ed1 k\u1ebft qu\u1ea3 c\u1ee7a m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1 khi k\u1ebft th\u00fac.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AuctionStartedResponse.java",
    "name": "AuctionStartedResponse.java",
    "filePath": "src/main/java/org/example/dto/response/AuctionStartedResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i x\u00e1c nh\u1eadn m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1 \u0111\u00e3 ch\u00ednh th\u1ee9c b\u1eaft \u0111\u1ea7u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/AuctionSummary.java",
    "name": "AuctionSummary.java",
    "filePath": "src/main/java/org/example/dto/response/AuctionSummary.java",
    "summary": "DTO l\u01b0u th\u00f4ng tin t\u00f3m t\u1eaft c\u01a1 b\u1ea3n v\u1ec1 m\u1ed9t cu\u1ed9c \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/BidHistoryResponse.java",
    "name": "BidHistoryResponse.java",
    "filePath": "src/main/java/org/example/dto/response/BidHistoryResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i ch\u1ee9a to\u00e0n b\u1ed9 l\u1ecbch s\u1eed \u0111\u1ea5u gi\u00e1 c\u1ee7a ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/BidUpdateResponse.java",
    "name": "BidUpdateResponse.java",
    "filePath": "src/main/java/org/example/dto/response/BidUpdateResponse.java",
    "summary": "DTO th\u00f4ng b\u00e1o khi c\u00f3 ng\u01b0\u1eddi d\u00f9ng \u0111\u1eb7t gi\u00e1 m\u1edbi trong cu\u1ed9c \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/ChangePasswordResponse.java",
    "name": "ChangePasswordResponse.java",
    "filePath": "src/main/java/org/example/dto/response/ChangePasswordResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i x\u00e1c nh\u1eadn tr\u1ea1ng th\u00e1i \u0111\u1ed5i m\u1eadt kh\u1ea9u.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/CreateAuctionResponse.java",
    "name": "CreateAuctionResponse.java",
    "filePath": "src/main/java/org/example/dto/response/CreateAuctionResponse.java",
    "summary": "DTO x\u00e1c nh\u1eadn ph\u1ea3n h\u1ed3i sau khi t\u1ea1o th\u00e0nh c\u00f4ng phi\u00ean \u0111\u1ea5u gi\u00e1 m\u1edbi.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/JoinResponse.java",
    "name": "JoinResponse.java",
    "filePath": "src/main/java/org/example/dto/response/JoinResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i sau khi ng\u01b0\u1eddi d\u00f9ng tham gia th\u00e0nh c\u00f4ng v\u00e0o m\u1ed9t cu\u1ed9c \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/LoginResponse.java",
    "name": "LoginResponse.java",
    "filePath": "src/main/java/org/example/dto/response/LoginResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i k\u1ebft qu\u1ea3 x\u00e1c th\u1ef1c ng\u01b0\u1eddi d\u00f9ng (\u0111\u0103ng nh\u1eadp).",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/dto/response/RegisterResponse.java",
    "name": "RegisterResponse.java",
    "filePath": "src/main/java/org/example/dto/response/RegisterResponse.java",
    "summary": "DTO ph\u1ea3n h\u1ed3i k\u1ebft qu\u1ea3 sau khi \u0111\u0103ng k\u00fd t\u00e0i kho\u1ea3n m\u1edbi.",
    "type": "file"
  },
  {
    "id": "file:src/main/java/org/example/SocketConsoleClient.java",
    "name": "SocketConsoleClient.java",
    "filePath": "src/main/java/org/example/SocketConsoleClient.java",
    "summary": "\u1ee8ng d\u1ee5ng client tr\u00ean d\u00f2ng l\u1ec7nh (Console) d\u00f9ng \u0111\u1ec3 g\u1eedi l\u1ec7nh v\u00e0 giao ti\u1ebfp v\u1edbi Server qua Socket.",
    "type": "file"
  },
  {
    "id": "config:src/main/resources/application-template.properties",
    "name": "application-template.properties",
    "filePath": "src/main/resources/application-template.properties",
    "summary": "T\u1ec7p c\u1ea5u h\u00ecnh m\u1eabu ch\u1ee9a c\u00e1c tham s\u1ed1 m\u1eb7c \u0111\u1ecbnh cho \u1ee9ng d\u1ee5ng.",
    "type": "config"
  },
  {
    "id": "config:src/main/resources/application.properties",
    "name": "application.properties",
    "filePath": "src/main/resources/application.properties",
    "summary": "T\u1ec7p c\u1ea5u h\u00ecnh ch\u00ednh c\u1ee7a \u1ee9ng d\u1ee5ng ch\u1ee9a c\u00e1c thi\u1ebft l\u1eadp m\u00f4i tr\u01b0\u1eddng v\u00e0 c\u01a1 s\u1edf d\u1eef li\u1ec7u.",
    "type": "config"
  },
  {
    "id": "file:src/main/resources/css/styles.css",
    "name": "styles.css",
    "filePath": "src/main/resources/css/styles.css",
    "summary": "T\u1ec7p stylesheet \u0111\u1ecbnh ngh\u0129a giao di\u1ec7n v\u00e0 ki\u1ec3u d\u00e1ng (CSS) cho c\u00e1c th\u00e0nh ph\u1ea7n UI c\u1ee7a \u1ee9ng d\u1ee5ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/AuctionDetail.fxml",
    "name": "AuctionDetail.fxml",
    "filePath": "src/main/resources/fxml/AuctionDetail.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML \u0111\u1ecbnh ngh\u0129a c\u1ea5u tr\u00fac m\u00e0n h\u00ecnh chi ti\u1ebft c\u1ee7a m\u1ed9t phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/BidHistory.fxml",
    "name": "BidHistory.fxml",
    "filePath": "src/main/resources/fxml/BidHistory.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML hi\u1ec3n th\u1ecb l\u1ecbch s\u1eed \u0111\u1eb7t gi\u00e1 c\u1ee7a ng\u01b0\u1eddi d\u00f9ng ho\u1eb7c phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/ConnectedLogin.fxml",
    "name": "ConnectedLogin.fxml",
    "filePath": "src/main/resources/fxml/ConnectedLogin.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML cho m\u00e0n h\u00ecnh \u0111\u0103ng nh\u1eadp k\u1ebft n\u1ed1i m\u1ea1ng ho\u1eb7c server.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/Dashboard.fxml",
    "name": "Dashboard.fxml",
    "filePath": "src/main/resources/fxml/Dashboard.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML \u0111\u1ecbnh ngh\u0129a m\u00e0n h\u00ecnh b\u1ea3ng \u0111i\u1ec1u khi\u1ec3n ch\u00ednh (dashboard) c\u1ee7a \u1ee9ng d\u1ee5ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/Deposit.fxml",
    "name": "Deposit.fxml",
    "filePath": "src/main/resources/fxml/Deposit.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML cung c\u1ea5p m\u00e0n h\u00ecnh n\u1ea1p ti\u1ec1n ho\u1eb7c \u0111\u1eb7t c\u1ecdc cho ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/Login.fxml",
    "name": "Login.fxml",
    "filePath": "src/main/resources/fxml/Login.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML \u0111\u1ecbnh ngh\u0129a m\u00e0n h\u00ecnh \u0111\u0103ng nh\u1eadp cho ng\u01b0\u1eddi d\u00f9ng h\u1ec7 th\u1ed1ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/ProductManagement.fxml",
    "name": "ProductManagement.fxml",
    "filePath": "src/main/resources/fxml/ProductManagement.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML d\u00e0nh cho vi\u1ec7c qu\u1ea3n l\u00fd c\u00e1c s\u1ea3n ph\u1ea9m trong h\u1ec7 th\u1ed1ng \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/Register.fxml",
    "name": "Register.fxml",
    "filePath": "src/main/resources/fxml/Register.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML hi\u1ec3n th\u1ecb m\u00e0n h\u00ecnh \u0111\u0103ng k\u00fd t\u00e0i kho\u1ea3n ng\u01b0\u1eddi d\u00f9ng m\u1edbi.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/fxml/UserManagement.fxml",
    "name": "UserManagement.fxml",
    "filePath": "src/main/resources/fxml/UserManagement.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML cho ch\u1ee9c n\u0103ng qu\u1ea3n tr\u1ecb v\u00e0 qu\u1ea3n l\u00fd danh s\u00e1ch ng\u01b0\u1eddi d\u00f9ng.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/META-INF/MANIFEST.MF",
    "name": "MANIFEST.MF",
    "filePath": "src/main/resources/META-INF/MANIFEST.MF",
    "summary": "T\u1ec7p k\u00ea khai (manifest) ch\u1ee9a si\u00eau d\u1eef li\u1ec7u cho g\u00f3i \u1ee9ng d\u1ee5ng Java.",
    "type": "file"
  },
  {
    "id": "file:src/main/resources/views/MainLayout.fxml",
    "name": "MainLayout.fxml",
    "filePath": "src/main/resources/views/MainLayout.fxml",
    "summary": "T\u1ec7p giao di\u1ec7n FXML x\u00e1c \u0111\u1ecbnh b\u1ed1 c\u1ee5c chung (layout) bao b\u1ecdc c\u00e1c m\u00e0n h\u00ecnh kh\u00e1c trong \u1ee9ng d\u1ee5ng.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/dao/item/ItemDAOTest.java",
    "name": "ItemDAOTest.java",
    "filePath": "src/test/java/org/example/dao/item/ItemDAOTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed Unit test x\u00e1c minh ch\u1ee9c n\u0103ng c\u1ee7a l\u1edbp ItemDAO, nh\u01b0 kh\u1edfi t\u1ea1o k\u1ebft n\u1ed1i database.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/AuctionTest.java",
    "name": "AuctionTest.java",
    "filePath": "src/test/java/org/example/entity/AuctionTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed x\u00e1c minh logic m\u00f4 h\u00ecnh \u0111\u1ed1i t\u01b0\u1ee3ng Auction (Phi\u00ean \u0111\u1ea5u gi\u00e1).",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/BaseEntityTest.java",
    "name": "BaseEntityTest.java",
    "filePath": "src/test/java/org/example/entity/BaseEntityTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed \u0111\u00e1nh gi\u00e1 t\u00ednh \u0111\u00fang \u0111\u1eafn c\u1ee7a th\u1ef1c th\u1ec3 c\u01a1 s\u1edf (BaseEntity) d\u00f9ng chung cho c\u00e1c m\u00f4 h\u00ecnh.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/BidTransactionTest.java",
    "name": "BidTransactionTest.java",
    "filePath": "src/test/java/org/example/entity/BidTransactionTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed \u0111\u1ea3m b\u1ea3o vi\u1ec7c l\u01b0u tr\u1eef v\u00e0 truy xu\u1ea5t c\u00e1c thu\u1ed9c t\u00ednh c\u1ee7a giao d\u1ecbch \u0111\u1ea5u gi\u00e1 (BidTransaction).",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/user/AdminTest.java",
    "name": "AdminTest.java",
    "filePath": "src/test/java/org/example/entity/user/AdminTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed x\u00e1c minh \u0111\u1ed1i t\u01b0\u1ee3ng Admin \u0111\u01b0\u1ee3c g\u00e1n quy\u1ec1n h\u1ee3p l\u00fd khi kh\u1edfi t\u1ea1o.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/user/BidderTest.java",
    "name": "BidderTest.java",
    "filePath": "src/test/java/org/example/entity/user/BidderTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed \u0111\u00e1nh gi\u00e1 t\u00ednh n\u0103ng t\u00e0i kho\u1ea3n ng\u01b0\u1eddi \u0111\u1ea5u gi\u00e1 (Bidder) bao g\u1ed3m s\u1ed1 d\u01b0 v\u00e0 vai tr\u00f2.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/user/SellerTest.java",
    "name": "SellerTest.java",
    "filePath": "src/test/java/org/example/entity/user/SellerTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed ch\u1ee9c n\u0103ng c\u1ee7a th\u1ef1c th\u1ec3 ng\u01b0\u1eddi b\u00e1n (Seller), ch\u1eb3ng h\u1ea1n nh\u01b0 h\u1ec7 s\u1ed1 \u0111\u00e1nh gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/entity/user/UserTest.java",
    "name": "UserTest.java",
    "filePath": "src/test/java/org/example/entity/user/UserTest.java",
    "summary": "T\u1ec7p Unit test t\u1eadp trung v\u00e0o l\u1edbp m\u00f4 h\u00ecnh ng\u01b0\u1eddi d\u00f9ng chung (User).",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/MainTest.java",
    "name": "MainTest.java",
    "filePath": "src/test/java/org/example/MainTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed \u0111\u00e1nh gi\u00e1 t\u00ednh \u0111\u00fang \u0111\u1eafn c\u1ee7a \u0111i\u1ec3m truy c\u1eadp \u1ee9ng d\u1ee5ng ch\u00ednh.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/network/AuctionServerTest.java",
    "name": "AuctionServerTest.java",
    "filePath": "src/test/java/org/example/network/AuctionServerTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed x\u1eed l\u00fd k\u1ebft n\u1ed1i m\u1ea1ng c\u1ee7a m\u00e1y ch\u1ee7 \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/service/AuctionServiceTest.java",
    "name": "AuctionServiceTest.java",
    "filePath": "src/test/java/org/example/service/AuctionServiceTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed cho d\u1ecbch v\u1ee5 x\u1eed l\u00fd logic kinh doanh trung t\u00e2m c\u1ee7a phi\u00ean \u0111\u1ea5u gi\u00e1.",
    "type": "file"
  },
  {
    "id": "file:src/test/java/org/example/utils/DatabaseConnectionTest.java",
    "name": "DatabaseConnectionTest.java",
    "filePath": "src/test/java/org/example/utils/DatabaseConnectionTest.java",
    "summary": "T\u1ec7p ki\u1ec3m th\u1eed \u0111\u01a1n v\u1ecb cho l\u1edbp DatabaseConnection, \u0111\u1ea3m b\u1ea3o kh\u1edfi t\u1ea1o k\u1ebft n\u1ed1i c\u01a1 s\u1edf d\u1eef li\u1ec7u \u0111\u00fang c\u00e1ch v\u00e0 \u00e1p d\u1ee5ng m\u1eabu thi\u1ebft k\u1ebf Singleton.",
    "type": "file"
  }
]
```

Layers:
```json
{json.dumps(layers_clean, indent=2)}
```

Edges (all types):
```json
{json.dumps(edges, indent=2)}
```
'''

with open(r"E:\HeThongDauGia\HeThongDauGia\tour_builder_dispatch.txt", "w", encoding="utf-8") as f:
    f.write(dispatch)
