package com.example.voucherservice.service;

import com.example.voucherservice.dto.response.ApiResponse;
import com.example.voucherservice.dto.request.UpdateVoucherQuantityRequest;
import com.example.voucherservice.dto.request.UpdateVoucherRequest;
import com.example.voucherservice.dto.request.VoucherRequest;
import com.example.voucherservice.dto.response.VoucherResponse;
import com.example.voucherservice.entity.Voucher;
import com.example.voucherservice.exception.AppException;
import com.example.voucherservice.enums.ErrorCode;
import com.example.voucherservice.mapper.VoucherMapper;
import com.example.voucherservice.repository.VoucherRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class VoucherService {
    VoucherRepository voucherRepository;
    VoucherMapper voucherMapper;

    final ExecutorService executorService = Executors.newSingleThreadExecutor();

     public ApiResponse<VoucherResponse> createVoucher(VoucherRequest request){
        var voucher = voucherMapper.toVoucher(request);
        return ApiResponse.<VoucherResponse>builder()
                .result(voucherMapper.toVoucherResponse(voucherRepository.save(voucher)))
                .build();
    }

    public ApiResponse<List<VoucherResponse>> getAllVouchersFreeShip(){

        return ApiResponse.<List<VoucherResponse>>builder()
                .result(voucherRepository.findByFreeShipTrue().stream().map(voucherMapper::toVoucherResponse).toList())
                .build();
    }

    public ApiResponse<VoucherResponse> updateVoucher(String voucherID, UpdateVoucherRequest request){
        var voucher = voucherRepository.findById(voucherID)
                .orElseThrow(()-> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        voucherMapper.updateVoucher(voucher, request);

        return ApiResponse.<VoucherResponse>builder()
                .result(voucherMapper.toVoucherResponse(voucherRepository.save(voucher)))
                .build();
    }

    public ApiResponse<VoucherResponse> useVoucher(String voucherID) {
        final Voucher[] voucher1 = new Voucher[1];
        // Đặt request vào hàng đợi (executor) để xử lý tuần tự
        executorService.submit(() -> {
            var voucher = voucherRepository.findById(voucherID)
                    .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

            log.info(voucher.getQuantity() + "");
            if (voucher.getQuantity() <= 0) {
                log.info("Lỗi");
                throw new AppException(ErrorCode.VOUCHER_OUT_OF_STOCK);
            }

            voucher.setQuantity(voucher.getQuantity() - 1);

            voucher1[0] = voucherRepository.save(voucher); // Lưu voucher với số lượng đã giảm
        });

        return ApiResponse.<VoucherResponse>builder()
                .result(voucherMapper.toVoucherResponse(voucher1[0])) // Trả về thông tin response theo yêu cầu
                .build();
    }

    public ApiResponse<VoucherResponse> updateVoucherQuantity(String voucherID, UpdateVoucherQuantityRequest request){
        var voucher = voucherRepository.findById(voucherID)
                .orElseThrow(()-> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        voucher.setQuantity(request.getQuantity());

        return ApiResponse.<VoucherResponse>builder()
                .result(voucherMapper.toVoucherResponse(voucherRepository.save(voucher)))
                .build();
    }

    public ApiResponse<?> deleteVoucher(String id){
        var voucher = voucherRepository.findById(id)
                .orElseThrow(()-> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        voucherRepository.delete(voucher);

        return ApiResponse.builder()
                .result("Voucher deleted successfully")
                .build();
    }

    public ApiResponse<List<VoucherResponse>> getAll(){
        return ApiResponse.<List<VoucherResponse>>builder()
                .result(voucherRepository.findAll().stream()
                        .map(voucherMapper::toVoucherResponse).toList())
                .build();
    }



    public ApiResponse<List<VoucherResponse>> getAllValidVouchers(){
        LocalDate localDate = LocalDate.now();
        List<Voucher> voucherList = voucherRepository
                .findAllByQuantityGreaterThanAndExpirationDateGreaterThanEqual(0,localDate);

        return ApiResponse.<List<VoucherResponse>>builder()
                .result(voucherList.stream().map(voucherMapper::toVoucherResponse).toList())
                .build();
    }
}
