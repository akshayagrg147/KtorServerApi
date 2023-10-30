package com.example.src.modal

data class HomeProducts(
    var productName: String = "",
    var orignal_price: String = "",
    var quantity: String = "",
    var productImage1: String? = null,
    var productImage2: String? = null,
    var productImage3: String? = null,
    var productId: String = "",
    var productBestSelling:Boolean=false,
    var productExclusiveSelling:Boolean=false,
    var quantityInstructionController: String? = null,

    var productDescription: String? = null,
    var selling_price: String? = null,
    var dashboardDisplay: Boolean? = false,
    var item_category_name: String? = null,
    var rating: List<subrating> = emptyList<subrating>(),
    var item_subcategory_name: String? = null,
    var changeTime: Double? = null,
    var pincode: String?=null
)
data class adminAcess(var email:String?=null, var password:String?=null, var pincode:String?=null, var name:String?=null, var price:String?=null, val city: String?=null, val deliveryContactNumber:String?=null, val fcm_token:String?=null,
                      var generateToken:String?=null)
data class adminAvailable(var pincode:String?=null,var price:String?=null,val city: String? ,val deliveryContactNumber:String)
data class exclusiveOffers(
    var productName: String = "",
    var orignal_price: String = "",
    var quantity: String = "",
    var ProductImage1: String? = null,
    var ProductImage2: String? = null,
    var ProductImage3: String? = null,
    var productId: String = "",
    var ProductDescription: String? = null,
    var selling_price: String? = null,
    var dashboardDisplay: Boolean? = false,
    var item_category_name: String? = null,
    var rating: List<subrating> = emptyList<subrating>(),
    var categoryType: Int = 0
)

data class bestSelling(
    var productName: String = "",
    var orignal_price: String = "",
    var quantity: String = "",
    var ProductImage1: String? = null,
    var ProductImage2: String? = null,
    var ProductImage3: String? = null,
    var productId: String?=null,
    var ProductDescription: String? = null,
    var selling_price: String? = null,
    var dashboardDisplay: Boolean? = false,
    var item_category_name: String? = null,
    var rating: List<subrating> = emptyList<subrating>(),
    var categoryType: Int = 0
)

data class SearchByProductId(val ProductId: String? = null,val pincode: String?)
data class CityAailibilty(val city: String? = null)
data class ProductCategory(
    val category: String? = null,
    val imageUrl: String? = null,
    val subCategoryList: List<SubCategoryItem>,
    val pincode:String?=null
)
data class BannerCategory(
    val bannercategory1: String? = null,
    val imageUrl1: String? = null,
    val bannercategory2: String? = null,
    val imageUrl2: String? = null,
    val bannercategory3: String? = null,
    val imageUrl3: String? = null,
    val subCategoryList: List<SubCategoryItem>?= emptyList(),
    val pincode:String?=null,
    var changetime:String?=null

)

data class SubCategoryItem(val name: String?, val subCategoryUrl: String? = null)

data class RelatedSerachByPriceAndCategory(val Price: String? = null, val category: String? = null,val pincode:String?=null)
data class UserRequest(val phone: String? = null,val pincode:String?=null)
data class PaginationPassing(val offset: Int? = 0, val limit: Int? = 0)
data class subrating(
    val remark: String? = null,
    val rating: String? = null,
    val name: String? = null,
    val customerId: String? = null
)

data class orderitem(
    val orderList: ArrayList<Orders>,
    val totalOrderValue: String? = null,
    val paymentmode: String? = null,
    var changeTime:Double,
    val address: String? = null,
    val mobilenumber: String,
    var createdDate: String,
    var orderId: String,
    var orderStatus:String?=null,
    var pincode:String?=null,
    var fcm_token:String?=null
)

data class Orders(
    val productId: String? = null,
    val productName: String? = null,
    val quantity: String? = null,
    val productprice: String? = null
)

data class Message(
    val message: String,
    val status: Boolean,
    val statusCode: Int
)

data class CheckNumberExist(val isMobileExist: Boolean, val statusCode: Int, val status: Boolean, val JwtToken: String)

data class JwtResponse(
    val token: String,
    val statusCode: Int,
    val message: String
)

data class CommonListResponse<T>(
    val itemData: List<T>,
    val statusCode: Int,
    val message: String
)
data class CommonClassResponse<T>(
    val response: T,
    val statusCode: Int,
    val message: String
)

data class CountResponse(
    val image: String,
    val name: String,
    val recentOrders: List<orderitem>,
    val CountResponse: List<ItemCount>,
    val statusCode: Int,
    val message: String
)

data class ItemCount(

    val name: String,
    val image: String,
    val count: Int
)


data class ProductResponseById<T>(
    val ProductResponse: T?,
    val statusCode: Int,
    val message: String
)

data class UserProfileResponse<T>(
    val name: T? = null,
    val statusCode: Int,
    val message: String


)

data class BookedOrders<T>(
    val ProductResponse: T?,

    val statusCode: Int,
    val message: String
)

data class DashboardHomeProductsModal(
    val ls: List<HomeProducts>?,
    val category: String,
    val categoryTitle: String
)
