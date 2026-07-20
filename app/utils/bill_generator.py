from io import BytesIO
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle

def generate_bill_pdf(bill):
    """
    Generates a professional, printable invoice PDF for a restaurant bill.
    Returns a BytesIO buffer stream containing the generated PDF.
    """
    buffer = BytesIO()
    
    # SimpleDocTemplate setup
    doc = SimpleDocTemplate(
        buffer, 
        pagesize=letter,
        rightMargin=40, 
        leftMargin=40,
        topMargin=40, 
        bottomMargin=40
    )
    story = []
    
    # Styles
    styles = getSampleStyleSheet()
    
    title_style = ParagraphStyle(
        'InvoiceTitle',
        parent=styles['Heading1'],
        fontName='Helvetica-Bold',
        fontSize=22,
        leading=26,
        textColor=colors.HexColor('#C0392B'),  # Deep Indian Copper Red
        alignment=1  # Center
    )
    
    normal_style = styles['Normal']
    bold_style = ParagraphStyle(
        'BoldText',
        parent=styles['Normal'],
        fontName='Helvetica-Bold'
    )
    
    right_align_normal = ParagraphStyle(
        'RightAlignNormal',
        parent=styles['Normal'],
        alignment=2  # Right
    )
    
    right_align_bold = ParagraphStyle(
        'RightAlignBold',
        parent=bold_style,
        alignment=2  # Right
    )
    
    # Header Section
    story.append(Paragraph(f"<b>{bill.branch.name}</b>", title_style))
    story.append(Spacer(1, 6))
    story.append(Paragraph(f"<i>{bill.branch.address or 'Indian Restaurant Main Street'}</i>", ParagraphStyle('Sub', parent=normal_style, alignment=1)))
    story.append(Paragraph(f"Phone: {bill.branch.phone or 'N/A'}", ParagraphStyle('Sub2', parent=normal_style, alignment=1)))
    story.append(Spacer(1, 15))
    
    # Invoice Metadata Grid
    details_data = [
        [
            Paragraph(f"<b>Bill No:</b> {bill.bill_id}", normal_style), 
            Paragraph(f"<b>Date:</b> {bill.bill_date.strftime('%d-%m-%Y')}", right_align_normal)
        ],
        [
            Paragraph(f"<b>Table:</b> {bill.table.table_id} (Cap: {bill.table.capacity})", normal_style), 
            Paragraph(f"<b>Time:</b> {bill.bill_time.strftime('%I:%M %p')}", right_align_normal)
        ],
        [
            Paragraph(f"<b>Billed By:</b> {bill.biller.username}", normal_style), 
            Paragraph(f"<b>Payment Status:</b> <font color='#27AE60'><b>{bill.payment_status.upper()}</b></font>", right_align_normal)
        ]
    ]
    
    details_table = Table(details_data, colWidths=[250, 250])
    details_table.setStyle(TableStyle([
        ('VALIGN', (0,0), (-1,-1), 'TOP'),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('TOPPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(details_table)
    story.append(Spacer(1, 15))
    
    # Invoice Items Table Header
    items_data = [
        [
            Paragraph("<b>Item Description</b>", bold_style),
            Paragraph("<b>Qty</b>", right_align_bold),
            Paragraph("<b>Unit Price</b>", right_align_bold),
            Paragraph("<b>Total Price</b>", right_align_bold)
        ]
    ]
    
    # Populating items
    for item in bill.order.items:
        items_data.append([
            Paragraph(item.menu_item.name, normal_style),
            Paragraph(str(item.quantity), right_align_normal),
            Paragraph(f"₹{item.unit_price:.2f}", right_align_normal),
            Paragraph(f"₹{item.total_price:.2f}", right_align_normal)
        ])
        
    # Append Calculations
    # Empty column spacers to align total labels right
    items_data.append([Paragraph("", normal_style), Paragraph("", normal_style), Paragraph("<b>Subtotal:</b>", right_align_normal), Paragraph(f"₹{bill.subtotal:.2f}", right_align_normal)])
    
    if bill.discount_amount > 0:
        items_data.append([Paragraph("", normal_style), Paragraph("", normal_style), Paragraph("<b>Discount:</b>", right_align_normal), Paragraph(f"-₹{bill.discount_amount:.2f}", right_align_normal)])
        
    items_data.append([Paragraph("", normal_style), Paragraph("", normal_style), Paragraph("<b>GST Amount:</b>", right_align_normal), Paragraph(f"₹{bill.tax_amount:.2f}", right_align_normal)])
    items_data.append([Paragraph("", normal_style), Paragraph("", normal_style), Paragraph("<b>Grand Total:</b>", right_align_bold), Paragraph(f"<b>₹{bill.total_amount:.2f}</b>", right_align_bold)])
    
    # Column width specifications
    items_table = Table(items_data, colWidths=[240, 50, 110, 100])
    items_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.HexColor('#F5B041')), # Saffron Accent Header
        ('TEXTCOLOR', (0,0), (-1,0), colors.whitesmoke),
        ('ALIGN', (0,0), (-1,-1), 'LEFT'),
        ('BOTTOMPADDING', (0,0), (-1,-1), 6),
        ('TOPPADDING', (0,0), (-1,-1), 6),
        ('GRID', (0,0), (-1,-5), 0.5, colors.grey), # Grid outline for item rows
        ('LINEBELOW', (2,-4), (-1,-1), 1, colors.HexColor('#C0392B')), # Red line above totals
        ('VALIGN', (0,0), (-1,-1), 'MIDDLE'),
    ]))
    story.append(items_table)
    story.append(Spacer(1, 15))
    
    # Payment modes details
    payment_txt = f"<b>Payment Mode:</b> {bill.payment_mode.upper()}"
    story.append(Paragraph(payment_txt, normal_style))
    
    if bill.notes:
        story.append(Spacer(1, 6))
        story.append(Paragraph(f"<b>Notes:</b> {bill.notes}", normal_style))
        
    story.append(Spacer(1, 25))
    story.append(Paragraph("<b>Thank you for dining with us! Visit again.</b>", ParagraphStyle('Footer', parent=normal_style, fontName='Helvetica-BoldOblique', alignment=1)))
    
    # Compile document
    doc.build(story)
    buffer.seek(0)
    return buffer
